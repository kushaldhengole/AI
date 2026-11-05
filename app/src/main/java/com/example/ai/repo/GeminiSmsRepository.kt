package com.example.ai.repo

import com.example.ai.BuildConfig
import com.example.ai.models.SmsExtractionResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSmsRepository @Inject constructor(
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
                    You are an expert financial SMS message parser. Extract transaction details from bank/UPI/card SMS strictly following the JSON schema:
                    {
                      "amount": number,
                      "transactionType": "DEBIT" or "CREDIT",
                      "merchantOrSender": string,
                      "paymentMethod": "UPI" or "CARD" or "NET_BANKING" or "CASH",
                      "category": "Food" or "Shopping" or "Bills" or "Salary" or "Transfer" or "General",
                      "date": "YYYY-MM-DD"
                    }
                    If transaction is debited, spent, paid, or sent, transactionType is "DEBIT".
                    If transaction is credited, deposited, received, or salary, transactionType is "CREDIT".
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun parseSmsMessage(smsText: String): Result<SmsExtractionResult> =
        withContext(Dispatchers.IO) {
            // 1. Try On-Device AI (Gemini Nano) first
            val onDeviceResult = onDeviceAiRepository.parseSmsOnDevice(smsText)
            if (onDeviceResult.isSuccess) {
                return@withContext onDeviceResult
            }

            // 2. Fallback to Cloud Gemini API if On-Device AI is not downloaded/supported
            runCatching {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    throw IllegalArgumentException("Gemini API key is not configured.")
                }

                val prompt = content {
                    text("Extract financial transaction details from this SMS: $smsText")
                }

                val response = generativeModel.generateContent(prompt)
                val rawJson = response.text
                    ?: throw IllegalStateException("Empty response from Gemini LLM")
                json.decodeFromString<SmsExtractionResult>(rawJson)
            }
        }
}
