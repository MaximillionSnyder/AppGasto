package com.example.appgasto.ui.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgasto.data.local.Receipt
import com.example.appgasto.data.receipts.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    val receipts: StateFlow<List<Receipt>> = receiptRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val count: StateFlow<Int> = receiptRepository.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun fileFor(receipt: Receipt): File = receiptRepository.fileFor(receipt)

    fun delete(receipt: Receipt) {
        viewModelScope.launch {
            receiptRepository.delete(receipt)
        }
    }
}
