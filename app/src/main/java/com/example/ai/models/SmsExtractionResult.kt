package com.example.ai.models

import kotlinx.serialization.Serializable

@Serializable
data class SmsExtractionResult(
    val amount: Double = 0.0,
    val transactionType: String = "DEBIT", // "DEBIT" or "CREDIT"
    val merchantOrSender: String = "Bank Transaction",
    val paymentMethod: String = "UPI", // "UPI", "CARD", "NET_BANKING", "CASH"
    val category: String = "General", // "Food", "Shopping", "Bills", "Salary", "Transfer"
    val date: String = ""
)
