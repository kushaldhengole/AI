package com.example.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.models.ReceiptExtractionResult
import com.example.ai.models.ReceiptLineItem
import com.example.ai.ui.theme.AiTheme

@Composable
fun ListView(
    receiptList: List<ReceiptExtractionResult>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(receiptList) { receipt ->
            ReceiptCardView(receipt)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListViewPreview() {
    AiTheme {
        ListView(
            receiptList = listOf(
                ReceiptExtractionResult(
                    merchantName = "Walmart Supercenter",
                    transactionDate = "2025-02-28",
                    currency = "$",
                    lineItems = listOf(
                        ReceiptLineItem("Organic Milk", 1.0, 4.49, 4.49)
                    ),
                    subtotal = 13.48,
                    tax = 1.08,
                    totalAmount = 14.56,
                    suggestedCategory = "Groceries"
                ),
                ReceiptExtractionResult(
                    merchantName = "Target Store",
                    transactionDate = "2025-02-27",
                    currency = "$",
                    lineItems = listOf(
                        ReceiptLineItem("Coffee Beans", 1.0, 12.99, 12.99)
                    ),
                    subtotal = 12.99,
                    tax = 1.04,
                    totalAmount = 14.03,
                    suggestedCategory = "Supplies"
                )
            )
        )
    }
}
