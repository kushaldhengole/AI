package com.example.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountName: String,
    val accountType: String, // "BANK", "UPI", "CREDIT_CARD", "CASH"
    val balance: Double,
    val accountNumberLast4: String? = null
)
