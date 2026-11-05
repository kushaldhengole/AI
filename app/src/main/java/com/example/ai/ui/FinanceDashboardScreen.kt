package com.example.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ai.data.TransactionEntity
import com.example.ai.ui.theme.AiTheme
import com.example.ai.viewModel.FinanceDashboardState
import com.example.ai.viewModel.FinanceManagerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    viewModel: FinanceManagerViewModel,
    onNavigateToScanReceipt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddTransactionDialog by remember { mutableStateOf(false) }

    // Launcher for SMS Permission
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncSmsMessages(context)
        }
    }

    FinanceDashboardContent(
        state = state,
        onAddTransactionClick = { showAddTransactionDialog = true },
        onSyncSmsClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.syncSmsMessages(context)
            } else {
                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        },
        modifier = modifier
    )

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { amount, type, paymentMethod, category, description, date ->
                viewModel.addTransaction(
                    amount = amount,
                    type = type,
                    paymentMethod = paymentMethod,
                    category = category,
                    description = description,
                    date = date
                )
                showAddTransactionDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardContent(
    state: FinanceDashboardState,
    onAddTransactionClick: () -> Unit,
    onSyncSmsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpenses = state.totalExpenses
    val totalIncome = state.totalIncome
    val monthlyTarget = 1000.0
    val progressFraction = (totalExpenses / monthlyTarget).coerceIn(0.0, 1.0).toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income & Expenses", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSyncSmsClick) {
                        Icon(Icons.Default.Sms, contentDescription = "Sync Bank SMS")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransactionClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Transaction") },
                text = { Text("Record Expense / Income") }
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
            // ====================================================
            // TOP CENTER SECTION: Overview Card with Progress
            // ====================================================
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
                            text = "Income vs Expenses Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Progress Indicator in Top Center
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
                                color = MaterialTheme.colorScheme.error,
                                strokeWidth = 14.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$ ${String.format(Locale.getDefault(), "%.2f", totalExpenses)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Total Debits",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Income vs Expense Badges & Sync SMS Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "+ $ ${String.format(Locale.getDefault(), "%.2f", totalIncome)}",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                            OutlinedButton(
                                onClick = onSyncSmsClick,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isSyncingSms) "Scanning..." else "Sync SMS AI")
                            }
                        }

                        state.syncMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ====================================================
            // BOTTOM SECTION: Transaction Sections
            // ====================================================
            if (state.transactions.isEmpty()) {
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
                                text = "No Debits or Credits Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Sync SMS AI' to auto-extract transactions from bank SMS, or add manually.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(state.transactions) { transaction ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TransactionSectionCard(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSectionCard(transaction: TransactionEntity) {
    val isCredit = transaction.transactionType.equals("CREDIT", ignoreCase = true)

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCredit) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (isCredit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = transaction.description.ifBlank { transaction.category },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Date: ${transaction.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${if (isCredit) "+" else "-"} $ ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCredit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Method: ${transaction.paymentMethod}") }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(transaction.category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, type: String, paymentMethod: String, category: String, description: String, date: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("DEBIT") } // "DEBIT" or "CREDIT"
    var paymentMethod by remember { mutableStateOf("UPI") } // "UPI", "CARD", "NET_BANKING", "CASH"
    var category by remember { mutableStateOf("Shopping") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2025-02-28") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Record Debit / Credit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Debit / Credit selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { transactionType = "DEBIT" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Debit (-)", color = if (transactionType == "DEBIT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    OutlinedButton(
                        onClick = { transactionType = "CREDIT" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Credit (+)", color = if (transactionType == "CREDIT") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Sender (e.g. Swiggy, Salary)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onConfirm(
                                    amount,
                                    transactionType,
                                    paymentMethod,
                                    category,
                                    description,
                                    date
                                )
                            }
                        }
                    ) {
                        Text("Save Transaction")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinanceDashboardPreview() {
    AiTheme {
        FinanceDashboardContent(
            state = FinanceDashboardState(
                transactions = listOf(
                    TransactionEntity(id = 1, accountId = 1, amount = 2500.00, transactionType = "CREDIT", paymentMethod = "NET_BANKING", category = "Salary", description = "Monthly Salary Credited", date = "2025-02-28"),
                    TransactionEntity(id = 2, accountId = 1, amount = 450.00, transactionType = "DEBIT", paymentMethod = "UPI", category = "Food", description = "Zomato Order Debited", date = "2025-02-28")
                ),
                totalIncome = 2500.00,
                totalExpenses = 450.00,
                isSyncingSms = false,
                syncMessage = null
            ),
            onAddTransactionClick = {},
            onSyncSmsClick = {}
        )
    }
}
