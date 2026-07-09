package com.example.appgasto.data.ocr

import android.net.Uri

interface ReceiptOcrService {
    suspend fun parseReceiptImage(imageUri: Uri): ReceiptData
}
