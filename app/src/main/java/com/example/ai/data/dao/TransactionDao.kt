package com.example.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ai.data.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM finance_transactions ORDER BY createdAt DESC")
    abstract fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM finance_transactions WHERE accountId = :accountId ORDER BY createdAt DESC")
    abstract fun observeTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM finance_transactions WHERE transactionType = 'CREDIT'")
    abstract fun observeTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM finance_transactions WHERE transactionType = 'DEBIT'")
    abstract fun observeTotalExpenses(): Flow<Double?>
}
