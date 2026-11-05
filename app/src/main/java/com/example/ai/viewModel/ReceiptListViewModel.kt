package com.example.ai.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.ReceiptWithItems
import com.example.ai.data.dao.ReceiptDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    receiptDao: ReceiptDao
) : ViewModel() {

    val savedReceiptsWithItems: StateFlow<List<ReceiptWithItems>> = receiptDao.observeAllReceiptsWithItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
