package com.example.appgasto.data.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class MLKitReceiptOcrService(
    private val context: Context,
    private val parser: ReceiptParser
) : ReceiptOcrService {

    override suspend fun parseReceiptImage(imageUri: Uri): ReceiptData = withContext(Dispatchers.IO) {
        val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw IOException("No se pudo leer la imagen del recibo")

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))
            parser.parse(result.text)
        } finally {
            recognizer.close()
        }
    }
}
