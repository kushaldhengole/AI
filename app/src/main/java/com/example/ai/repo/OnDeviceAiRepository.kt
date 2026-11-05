package com.example.ai.repo

import android.graphics.Bitmap
import com.example.ai.models.ReceiptExtractionResult
import com.example.ai.models.SmsExtractionResult
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceAiRepository @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val generativeModel by lazy {
        Generation.getClient()
    }

    suspend fun isAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                generativeModel.checkStatus() == 1
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun parseSmsOnDevice(smsText: String): Result<SmsExtractionResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val prompt = """
                    You are a local financial SMS parser running on-device. Extract transaction details as JSON:
                    {
                      "amount": number,
                      "transactionType": "DEBIT" or "CREDIT",
                      "merchantOrSender": string,
                      "paymentMethod": "UPI" or "CARD" or "NET_BANKING" or "CASH",
                      "category": "Food" or "Shopping" or "Bills" or "Salary" or "Transfer" or "General",
                      "date": "YYYY-MM-DD"
                    }
                    SMS text: $smsText
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val rawJson = response.candidates.firstOrNull()?.text
                    ?: throw IllegalStateException("No text generated from On-Device AI.")
                json.decodeFromString<SmsExtractionResult>(rawJson)
            }
        }

    suspend fun parseReceiptOnDevice(bitmap: Bitmap): Result<ReceiptExtractionResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val promptText = """
                    Extract receipt details as JSON:
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
                """.trimIndent()

                val request = GenerateContentRequest.Builder(
                    ImagePart(bitmap),
                    TextPart(promptText)
                ).build()

                val response = generativeModel.generateContent(request)
                val rawJson = response.candidates.firstOrNull()?.text
                    ?: throw IllegalStateException("No text generated from On-Device AI.")

                json.decodeFromString<ReceiptExtractionResult>(rawJson)
            }
        }
}
