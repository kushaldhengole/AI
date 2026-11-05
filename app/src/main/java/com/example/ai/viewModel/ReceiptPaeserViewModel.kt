package com.example.ai.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.ReceiptEntity
import com.example.ai.data.ReceiptItemEntity
import com.example.ai.data.dao.ReceiptDao
import com.example.ai.models.ReceiptExtractionResult
import com.example.ai.models.ReceiptScanUiState
import com.example.ai.repo.GeminiReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReceiptScannerViewModel @Inject constructor(
    private val geminiRepository: GeminiReceiptRepository,
    private val receiptDao: ReceiptDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptScanUiState>(ReceiptScanUiState.Idle)
    val uiState: StateFlow<ReceiptScanUiState> = _uiState.asStateFlow()

    fun processReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = ReceiptScanUiState.Processing
            geminiRepository.parseReceipt(bitmap)
                .onSuccess { result ->
                    saveToLocalDb(result)
                    _uiState.value = ReceiptScanUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = ReceiptScanUiState.Error(error.localizedMessage ?: "Failed to process receipt")
                }
        }
    }

    private suspend fun saveToLocalDb(result: ReceiptExtractionResult) {
        val entity = ReceiptEntity(
            merchantName = result.merchantName,
            date = result.transactionDate,
            totalAmount = result.totalAmount,
            tax = result.tax,
            currency = result.currency,
            category = result.suggestedCategory
        )
        val items = result.lineItems.map {
            ReceiptItemEntity(
                receiptId = 0,
                itemName = it.itemName,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                totalPrice = it.totalPrice
            )
        }
        receiptDao.insertFullReceipt(entity, items)
    }
}