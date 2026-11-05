package com.example.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.models.ReceiptExtractionResult
import com.example.ai.models.ReceiptLineItem
import com.example.ai.ui.theme.AiTheme
import java.util.Locale

@Composable
fun ReceiptResultView(
    receipt: ReceiptExtractionResult,
    onConfirm: () -> Unit
) {
    val currencyStr = receipt.currency
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        item {
           ReceiptCardView(receipt)
        }

        // Section Title
        item {
            Text(
                text = "Itemized Breakdown (${receipt.lineItems.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Items List
        items(receipt.lineItems) { item: ReceiptLineItem ->
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.itemName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Qty: ${item.quantity} × $currencyStr ${item.unitPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$currencyStr ${String.format(Locale.getDefault(), "%.2f", item.totalPrice)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Confirmation Button
        item {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saved to Expenses")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptResultViewPreview() {
    AiTheme {
        ReceiptResultView(
            receipt = ReceiptExtractionResult(
                merchantName = "Walmart Supercenter",
                transactionDate = "2025-02-28",
                currency = "$",
                lineItems = listOf(
                    ReceiptLineItem("Organic Whole Milk 1 Gal", 1.0, 4.49, 4.49),
                    ReceiptLineItem("Avocados (Bag of 5)", 1.0, 3.99, 3.99),
                    ReceiptLineItem("Whole Wheat Bread", 2.0, 2.50, 5.00)
                ),
                subtotal = 13.48,
                tax = 1.08,
                totalAmount = 14.56,
                suggestedCategory = "Groceries"
            ),
            onConfirm = {}
        )
    }
}

@Composable
fun ReceiptCardView(receipt: ReceiptExtractionResult) {
    val currencyStr = receipt.currency
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receipt.merchantName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(receipt.suggestedCategory) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${receipt.transactionDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount", fontWeight = FontWeight.Bold)
                Text(
                    text = "$currencyStr ${String.format(Locale.getDefault(), "%.2f", receipt.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}