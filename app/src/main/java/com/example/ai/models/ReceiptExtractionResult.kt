package com.example.ai.models
import kotlinx.serialization.Serializable

    @Serializable
    data class ReceiptExtractionResult(
        val merchantName: String,
        val transactionDate: String, // YYYY-MM-DD
        val currency: String,
        val lineItems: List<ReceiptLineItem>,
        val subtotal: Double,
        val tax: Double,
        val totalAmount: Double,
        val suggestedCategory: String // "Groceries", "Dining", "Electronics", etc.
    )

    @Serializable
    data class ReceiptLineItem(
        val itemName: String,
        val quantity: Double = 1.0,
        val unitPrice: Double,
        val totalPrice: Double
    )
