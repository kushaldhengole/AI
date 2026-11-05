package com.example.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ai.data.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun observeAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    @Query("UPDATE bank_accounts SET balance = balance + :amountDelta WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: Long, amountDelta: Double)
}
