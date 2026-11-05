package com.example.ai.models

sealed interface ReceiptScanUiState {
    object Idle : ReceiptScanUiState
    object Processing : ReceiptScanUiState
    data class Success(val receipt: ReceiptExtractionResult) : ReceiptScanUiState
    data class Error(val message: String) : ReceiptScanUiState
}