package com.example.ai.repo

import android.graphics.Bitmap
import com.example.ai.BuildConfig
import com.example.ai.models.ReceiptExtractionResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiReceiptRepository @Inject constructor(
    private val onDeviceAiRepository: OnDeviceAiRepository
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.1f
                responseMimeType = "application/json"
            },
            systemInstruction = content {
                text(
                    """
                    You are an expert financial receipt parser. Extract receipt details strictly 
                    following the JSON schema:
                    {
                      "merchantName": string,
                      "transactionDate": "YYYY-MM-DD",
                      "currency": string,
                      "lineItems": [{"itemName": string, "quantity": number, "unitPrice": number, "totalPrice": number}],
                      "subtotal": number,
                      "tax": number,
                      "totalAmount": number,
                      "suggestedCategory": string
                    }
                    Normalize dates and default missing quantities to 1.0.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun parseReceipt(bitmap: Bitmap): Result<ReceiptExtractionResult> =
        withContext(Dispatchers.IO) {
            // 1. Try On-Device AI (Gemini Nano) first
            val onDeviceResult = onDeviceAiRepository.parseReceiptOnDevice(bitmap)
            if (onDeviceResult.isSuccess) {
                return@withContext onDeviceResult
            }

            // 2. Fallback to Cloud Gemini API
            runCatching {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    throw IllegalArgumentException("Gemini API key is missing. Please set GEMINI_API_KEY=your_key in local.properties and rebuild.")
                }

                val prompt = content {
                    image(bitmap)
                    text("Extract all purchase details, itemized entries, and taxes from this receipt image.")
                }

                val response = generativeModel.generateContent(prompt)
                val rawJson = response.text
                    ?: throw IllegalStateException("Empty response received from Gemini.")
                json.decodeFromString<ReceiptExtractionResult>(rawJson)
            }
        }
}
