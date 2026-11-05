package com.example.ai.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.AccountEntity
import com.example.ai.data.TransactionEntity
import com.example.ai.data.dao.AccountDao
import com.example.ai.data.dao.TransactionDao
import com.example.ai.repo.GeminiSmsRepository
import com.example.ai.utils.SmsReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class FinanceDashboardState(
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val isSyncingSms: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class FinanceManagerViewModel @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val geminiSmsRepository: GeminiSmsRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FinanceDashboardState> = combine(
        accountDao.observeAllAccounts(),
        transactionDao.observeAllTransactions(),
        transactionDao.observeTotalIncome(),
        transactionDao.observeTotalExpenses(),
        _isSyncing,
        _syncMessage
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val accounts = array[0] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val transactions = array[1] as List<TransactionEntity>
        val income = array[2] as Double?
        val expenses = array[3] as Double?
        val isSyncing = array[4] as Boolean
        val message = array[5] as String?

        FinanceDashboardState(
            accounts = accounts,
            transactions = transactions,
            totalIncome = income ?: 0.0,
            totalExpenses = expenses ?: 0.0,
            isSyncingSms = isSyncing,
            syncMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceDashboardState()
    )

    fun syncSmsMessages(context: Context) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Reading financial SMS inbox..."

            val smsList = SmsReader.readFinancialSms(context, limit = 10)
            if (smsList.isEmpty()) {
                _syncMessage.value = "No financial SMS messages found in inbox."
                _isSyncing.value = false
                return@launch
            }

            var defaultAccount = accountDao.getAccountById(1)
            if (defaultAccount == null) {
                val accId = accountDao.insertAccount(
                    AccountEntity(
                        accountName = "Primary Bank / UPI",
                        accountType = "BANK",
                        balance = 10000.0,
                        accountNumberLast4 = "1234"
                    )
                )
                defaultAccount = accountDao.getAccountById(accId)
            }

            val targetAccountId = defaultAccount?.id ?: 1L
            var parsedCount = 0

            smsList.forEach { sms ->
                _syncMessage.value = "Parsing bank SMS with Gemini LLM..."
                geminiSmsRepository.parseSmsMessage(sms.body)
                    .onSuccess { result ->
                        val dateFormatted = result.date.ifBlank {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sms.dateSent))
                        }
                        val transaction = TransactionEntity(
                            accountId = targetAccountId,
                            amount = result.amount,
                            transactionType = result.transactionType.uppercase(),
                            paymentMethod = result.paymentMethod,
                            category = result.category,
                            description = result.merchantOrSender,
                            date = dateFormatted
                        )
                        transactionDao.insertTransaction(transaction)

                        val delta = if (result.transactionType.equals("CREDIT", ignoreCase = true)) result.amount else -result.amount
                        accountDao.updateAccountBalance(targetAccountId, delta)
                        parsedCount++
                    }
                    .onFailure {
                        // Continue to next SMS
                    }
            }

            _syncMessage.value = "Synced $parsedCount transactions from SMS with Gemini AI!"
            _isSyncing.value = false
        }
    }

    fun addTransaction(
        amount: Double,
        type: String, // "CREDIT" or "DEBIT"
        paymentMethod: String,
        category: String,
        description: String,
        date: String
    ) {
        viewModelScope.launch {
            var defaultAccount = accountDao.getAccountById(1)
            if (defaultAccount == null) {
                val accId = accountDao.insertAccount(
                    AccountEntity(
                        accountName = "Primary Bank / UPI",
                        accountType = "BANK",
                        balance = 10000.0,
                        accountNumberLast4 = "1234"
                    )
                )
                defaultAccount = accountDao.getAccountById(accId)
            }

            val targetAccountId = defaultAccount?.id ?: 1L
            val transaction = TransactionEntity(
                accountId = targetAccountId,
                amount = amount,
                transactionType = type,
                paymentMethod = paymentMethod,
                category = category,
                description = description,
                date = date
            )
            transactionDao.insertTransaction(transaction)

            val delta = if (type == "CREDIT") amount else -amount
            accountDao.updateAccountBalance(targetAccountId, delta)
        }
    }
}
