package com.example.appgasto.data.receipts

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.appgasto.data.local.Receipt
import com.example.appgasto.data.local.ReceiptDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las fotos de recibos SOLO en almacenamiento interno privado:
 * filesDir/receipts/*.jpg (+ .nomedia). Nunca MediaStore ni carpeta pública,
 * por lo que no aparecen en la galería del usuario.
 *
 * Flujo "solo al guardar el gasto":
 * 1. Al escanear -> [stageTemp] copia a cacheDir/receipt_tmp (la Uri del
 *    escáner es temporal y puede caducar).
 * 2. Al pulsar Guardar -> [commitTemp] mueve a filesDir/receipts e inserta fila.
 * 3. Si se sale sin guardar -> [discardTemp] borra el temporal.
 */
@Singleton
class ReceiptRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receiptDao: ReceiptDao
) {
    companion object {
        const val RECEIPTS_DIR = "receipts"
        private const val TMP_DIR = "receipt_tmp"
        private const val MAX_SIDE_PX = 1920
        private const val JPEG_QUALITY = 85
    }

    fun getAll(): Flow<List<Receipt>> = receiptDao.getAll()

    fun getCount(): Flow<Int> = receiptDao.getCount()

    fun receiptsDir(): File = File(context.filesDir, RECEIPTS_DIR).apply {
        if (!exists()) mkdirs()
        // Cinturón extra: evita que algún indexador de medios la incluya.
        val nomedia = File(this, ".nomedia")
        if (!nomedia.exists()) runCatching { nomedia.createNewFile() }
    }

    private fun tmpDir(): File = File(context.cacheDir, TMP_DIR).apply {
        if (!exists()) mkdirs()
    }

    fun fileFor(receipt: Receipt): File = File(receiptsDir(), receipt.fileName)

    fun tmpFileFor(tmp: File): File = tmp

    /** Copia la Uri del escáner/galería a un temporal privado y la comprime. */
    suspend fun stageTemp(sourceUri: Uri): File = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalStateException("No se pudo leer la imagen del recibo")
        val rawBytes = input.use { it.readBytes() }

        val bitmap = decodeSampled(rawBytes, MAX_SIDE_PX)
            ?: throw IllegalStateException("No se pudo leer la imagen del recibo")

        val tmp = File(tmpDir(), "tmp_${UUID.randomUUID()}.jpg")
        FileOutputStream(tmp).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.flush()
        }
        if (!bitmap.isRecycled) bitmap.recycle()
        tmp
    }

    data class Snapshot(
        val createdAt: LocalDateTime,
        val amount: Double?,
        val currency: String?,
        val merchant: String?,
        val expenseId: Long?
    )

    /**
     * Mueve el temporal al archivo definitivo y crea la fila.
     * Si falla el insert, intenta borrar el fichero movido.
     */
    suspend fun commitTemp(tmp: File?, snapshot: Snapshot): Receipt? = withContext(Dispatchers.IO) {
        if (tmp == null || !tmp.exists()) return@withContext null
        receiptsDir()
        val dest = File(receiptsDir(), "receipt_${UUID.randomUUID()}.jpg")
        val moved = runCatching {
            tmp.copyTo(dest, overwrite = true)
            tmp.delete()
            true
        }.getOrDefault(false)
        if (!moved || !dest.exists()) return@withContext null
        val receipt = Receipt(
            fileName = dest.name,
            createdAt = snapshot.createdAt,
            amount = snapshot.amount,
            currency = snapshot.currency,
            merchant = snapshot.merchant?.takeIf { it.isNotBlank() },
            expenseId = snapshot.expenseId
        )
        return@withContext try {
            val id = receiptDao.insert(receipt)
            receipt.copy(id = id)
        } catch (e: Exception) {
            runCatching { dest.delete() }
            throw e
        }
    }

    suspend fun discardTemp(tmp: File?) = withContext(Dispatchers.IO) {
        runCatching { if (tmp != null && tmp.exists()) tmp.delete() }
    }

    /** Borra fila + fichero. El borrado de gastos NO llama aquí (las fotos se conservan). */
    suspend fun delete(receipt: Receipt) = withContext(Dispatchers.IO) {
        receiptDao.deleteById(receipt.id)
        runCatching { File(receiptsDir(), receipt.fileName).delete() }
    }

    private fun decodeSampled(bytes: ByteArray, maxSide: Int): android.graphics.Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}
