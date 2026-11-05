package com.example.ai.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val date: String,
    val totalAmount: Double,
    val tax: Double,
    val currency: String,
    val category: String,
    val localImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

