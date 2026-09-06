package com.example.appgasto.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Archivo privado de recibos escaneados.
 * La foto vive en filesDir/receipts/<fileName> (nunca en galería/MediaStore).
 * La fila sobrevive al borrado del gasto (sin FK en cascada).
 */
@Entity(
    tableName = "receipts",
    indices = [Index("createdAt")]
)
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val amount: Double? = null,
    val currency: String? = null,
    val merchant: String? = null,
    /** Solo informativo: no se borra en cascada. */
    val expenseId: Long? = null
)
