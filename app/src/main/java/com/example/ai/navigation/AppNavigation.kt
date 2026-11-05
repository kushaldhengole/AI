package com.example.ai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.ui.FinanceDashboardScreen
import com.example.ai.ui.ReceiptListScreen
import com.example.ai.ui.ReceiptScannerScreen
import com.example.ai.viewModel.FinanceManagerViewModel
import com.example.ai.viewModel.ReceiptListViewModel
import com.example.ai.viewModel.ReceiptScannerViewModel

sealed class Screen(val route: String, val title: String) {
    object FinanceDashboard : Screen("finance_dashboard", "Finance")
    object ReceiptList : Screen("receipt_list", "Receipts")
    object AddReceipt : Screen("add_receipt", "Scan")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.AddReceipt.route) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance") },
                        label = { Text("Accounts & Finance") },
                        selected = currentRoute == Screen.FinanceDashboard.route,
                        onClick = {
                            navController.navigate(Screen.FinanceDashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Receipts") },
                        label = { Text("Receipts") },
                        selected = currentRoute == Screen.ReceiptList.route,
                        onClick = {
                            navController.navigate(Screen.ReceiptList.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FinanceDashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Finance Dashboard (Bank Accounts, UPI, Card Debits & Credits)
            composable(Screen.FinanceDashboard.route) {
                val viewModel: FinanceManagerViewModel = hiltViewModel()
                FinanceDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToScanReceipt = {
                        navController.navigate(Screen.AddReceipt.route)
                    }
                )
            }

            // Saved Receipts List Screen
            composable(Screen.ReceiptList.route) {
                val viewModel: ReceiptListViewModel = hiltViewModel()
                ReceiptListScreen(
                    viewModel = viewModel,
                    onAddReceiptClick = {
                        navController.navigate(Screen.AddReceipt.route)
                    }
                )
            }

            // Add / Scan Receipt Screen with Gemini
            composable(Screen.AddReceipt.route) {
                val viewModel: ReceiptScannerViewModel = hiltViewModel()
                ReceiptScannerScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
