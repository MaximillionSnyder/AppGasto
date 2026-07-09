package com.example.appgasto.di

import android.content.Context
import com.example.appgasto.data.ocr.MLKitReceiptOcrService
import com.example.appgasto.data.ocr.ReceiptOcrService
import com.example.appgasto.data.ocr.ReceiptParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    @Singleton
    fun provideReceiptParser(): ReceiptParser = ReceiptParser

    @Provides
    @Singleton
    fun provideReceiptOcrService(
        @ApplicationContext context: Context,
        parser: ReceiptParser
    ): ReceiptOcrService = MLKitReceiptOcrService(context, parser)
}
