package com.example.appgasto.data.ocr

import java.time.LocalDate

data class ReceiptData(
    val total: String? = null,
    val date: LocalDate? = null,
    val merchant: String? = null,
    val currencyCode: String? = null
)
