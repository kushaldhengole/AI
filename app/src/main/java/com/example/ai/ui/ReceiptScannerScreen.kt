package com.example.ai.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.ai.models.ReceiptExtractionResult
import com.example.ai.models.ReceiptLineItem
import com.example.ai.models.ReceiptScanUiState
import com.example.ai.ui.theme.AiTheme
import com.example.ai.viewModel.ReceiptScannerViewModel

@Composable
fun ReceiptScannerScreen(
    viewModel: ReceiptScannerViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uriToBitmap(it)
            viewModel.processReceipt(bitmap)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.processReceipt(it) }
    }

    ReceiptScannerContent(
        uiState = uiState,
        onTakePhoto = { cameraLauncher.launch(null) },
        onPickGallery = { galleryLauncher.launch("image/*") },
        onConfirm = {
            onNavigateBack?.invoke()
        },
        onRetry = {},
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerContent(
    uiState: ReceiptScanUiState,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            if (uiState !is ReceiptScanUiState.Processing) {
                BottomActionBar(
                    onTakePhoto = onTakePhoto,
                    onPickGallery = onPickGallery
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ReceiptStateTransition"
            ) { state ->
                when (state) {
                    is ReceiptScanUiState.Idle -> IdleEmptyView()
                    is ReceiptScanUiState.Processing -> ProcessingView()
                    is ReceiptScanUiState.Error -> ErrorView(
                        errorMessage = state.message,
                        onRetry = onRetry
                    )
                    is ReceiptScanUiState.Success -> ReceiptResultView(
                        receipt = state.receipt,
                        onConfirm = onConfirm
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptScannerScreenIdlePreview() {
    AiTheme {
        ReceiptScannerContent(
            uiState = ReceiptScanUiState.Idle,
            onTakePhoto = {},
            onPickGallery = {},
            onConfirm = {},
            onRetry = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptScannerScreenSuccessPreview() {
    AiTheme {
        ReceiptScannerContent(
            uiState = ReceiptScanUiState.Success(
                receipt = ReceiptExtractionResult(
                    merchantName = "Target Store #1234",
                    transactionDate = "2025-02-28",
                    currency = "$",
                    lineItems = listOf(
                        ReceiptLineItem("Coffee Beans 12oz", 1.0, 12.99, 12.99),
                        ReceiptLineItem("Almond Milk 64oz", 2.0, 3.49, 6.98)
                    ),
                    subtotal = 19.97,
                    tax = 1.60,
                    totalAmount = 21.57,
                    suggestedCategory = "Groceries"
                )
            ),
            onTakePhoto = {},
            onPickGallery = {},
            onConfirm = {},
            onRetry = {},
            onNavigateBack = {}
        )
    }
}
