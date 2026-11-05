package com.example.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ai.data.ReceiptEntity
import com.example.ai.data.ReceiptItemEntity
import com.example.ai.data.ReceiptWithItems
import com.example.ai.ui.theme.AiTheme
import com.example.ai.viewModel.ReceiptListViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    viewModel: ReceiptListViewModel,
    onAddReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val receiptsWithItems by viewModel.savedReceiptsWithItems.collectAsState()

    ReceiptListContent(
        receiptsWithItems = receiptsWithItems,
        onAddReceiptClick = onAddReceiptClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListContent(
    receiptsWithItems: List<ReceiptWithItems>,
    onAddReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpense = receiptsWithItems.sumOf { it.receipt.totalAmount }
    val totalItemsCount = receiptsWithItems.sumOf { it.items.size }
    val monthlyTarget = 500.0
    val progressFraction = (totalExpense / monthlyTarget).coerceIn(0.0, 1.0).toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Receipts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReceiptClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Receipt") },
                text = { Text("Scan Receipt") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // TOP CENTER SECTION: Overview Card with Progress
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Monthly Spending Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Progress Indicator at top center
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1.0f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                strokeWidth = 14.dp
                            )
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 14.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$ ${String.format(Locale.getDefault(), "%.2f", totalExpense)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Summary Stats Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("${receiptsWithItems.size} Receipts") }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text("$totalItemsCount Items") }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // BOTTOM SECTION: Sections of each receipt
            // ==========================================
            if (receiptsWithItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Receipts Saved Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Scan Receipt' to extract itemized costs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(receiptsWithItems, key = { it.receipt.id }) { receiptWithItems ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReceiptSectionCard(receiptWithItems = receiptWithItems)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptSectionCard(receiptWithItems: ReceiptWithItems) {
    val receipt = receiptWithItems.receipt
    val items = receiptWithItems.items

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Receipt Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = receipt.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Date: ${receipt.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${receipt.currency} ${String.format(Locale.getDefault(), "%.2f", receipt.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (items.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Items Section (${items.size}):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Itemized breakdown section for each receipt item
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• ${item.itemName} (x${item.quantity.toInt()})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${receipt.currency} ${String.format(Locale.getDefault(), "%.2f", item.totalPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptListScreenPreview() {
    AiTheme {
        ReceiptListContent(
            receiptsWithItems = listOf(
                ReceiptWithItems(
                    receipt = ReceiptEntity(
                        id = 1,
                        merchantName = "Walmart Supercenter",
                        date = "2025-02-28",
                        totalAmount = 14.56,
                        tax = 1.08,
                        currency = "$",
                        category = "Groceries"
                    ),
                    items = listOf(
                        ReceiptItemEntity(itemId = 1, receiptId = 1, itemName = "Organic Milk", quantity = 1.0, unitPrice = 4.49, totalPrice = 4.49),
                        ReceiptItemEntity(itemId = 2, receiptId = 1, itemName = "Avocados 5ct", quantity = 1.0, unitPrice = 3.99, totalPrice = 3.99),
                        ReceiptItemEntity(itemId = 3, receiptId = 1, itemName = "Whole Bread", quantity = 2.0, unitPrice = 2.50, totalPrice = 5.00)
                    )
                ),
                ReceiptWithItems(
                    receipt = ReceiptEntity(
                        id = 2,
                        merchantName = "Target Store",
                        date = "2025-02-27",
                        totalAmount = 21.57,
                        tax = 1.60,
                        currency = "$",
                        category = "Shopping"
                    ),
                    items = listOf(
                        ReceiptItemEntity(itemId = 4, receiptId = 2, itemName = "Coffee Beans", quantity = 1.0, unitPrice = 12.99, totalPrice = 12.99),
                        ReceiptItemEntity(itemId = 5, receiptId = 2, itemName = "Almond Milk", quantity = 2.0, unitPrice = 3.49, totalPrice = 6.98)
                    )
                )
            ),
            onAddReceiptClick = {}
        )
    }
}
