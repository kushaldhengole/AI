package com.example.ai.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ai.data.dao.AccountDao
import com.example.ai.data.dao.ReceiptDao
import com.example.ai.data.dao.TransactionDao

@Database(
    entities = [
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        AccountEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}
