package com.example.ai.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "receipt_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["receiptId"])]
)
data class ReceiptItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val receiptId: Long,
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)