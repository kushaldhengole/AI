package com.example.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.ai.data.ReceiptEntity
import com.example.ai.data.ReceiptItemEntity
import com.example.ai.data.ReceiptWithItems
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItems(items: List<ReceiptItemEntity>)

    @Transaction
    open suspend fun insertFullReceipt(receipt: ReceiptEntity, items: List<ReceiptItemEntity>) {
        val receiptId = insertReceipt(receipt)
        val linkedItems = items.map { it.copy(receiptId = receiptId) }
        insertItems(linkedItems)
    }

    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    abstract fun observeAllReceipts(): Flow<List<ReceiptEntity>>

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    abstract fun observeAllReceiptsWithItems(): Flow<List<ReceiptWithItems>>
}
