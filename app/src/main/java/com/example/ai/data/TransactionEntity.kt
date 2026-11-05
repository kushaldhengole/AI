package com.example.ai.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "finance_transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val amount: Double,
    val transactionType: String, // "CREDIT" or "DEBIT"
    val paymentMethod: String, // "UPI", "CARD", "NET_BANKING", "CASH"
    val category: String, // "Food", "Shopping", "Salary", "Bills", "Transfer"
    val description: String,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)
