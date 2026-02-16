package com.questua.app.presentation.monetization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.UnlockRequirement
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.delay

val QuestuaGold = Color(0xFFFFC107)
val QuestuaGreen = Color(0xFF4CAF50)

@Composable
fun UnlockPreviewScreen(
    onNavigateBack: () -> Unit,
    onContentUnlocked: (String, String) -> Unit,
    viewModel: UnlockPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val paymentSheet = rememberPaymentSheet(paymentResultCallback = viewModel::onPaymentSheetResult)

    LaunchedEffect(state.clientSecret) {
        state.clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = secret,
                configuration = PaymentSheet.Configuration("Questua App")
            )
        }
    }

    LaunchedEffect(state.showSuccessPopup, state.isUnlocked) {
        if (state.showSuccessPopup && state.isUnlocked) {
            delay(2500)
            viewModel.dismissSuccessPopup()
            onContentUnlocked(viewModel.contentId, viewModel.contentType)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (state.isUnlocked) QuestuaGreen.copy(alpha = 0.2f) else QuestuaGold.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.isUnlocked) {
                        UnlockedView(onStartClick = {
                            onContentUnlocked(viewModel.contentId, viewModel.contentType)
                        })
                    } else {
                        LockedView(
                            state = state,
                            onBuyClick = { productId -> viewModel.initiatePurchase(productId) }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    QuestuaButton(
                        text = "Voltar",
                        onClick = onNavigateBack,
                        isSecondary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (state.showSuccessPopup) {
                Dialog(onDismissRequest = { }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = QuestuaGreen,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Compra Confirmada!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aguarde enquanto preparamos sua aventura...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            if (state.isUnlocked) {
                                Text("Pronto! Redirecionando...", color = QuestuaGreen, fontWeight = FontWeight.Bold)
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }

            if (state.error != null && !state.showSuccessPopup) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 60.dp)
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UnlockedView(onStartClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(QuestuaGreen.copy(alpha = 0.1f), CircleShape)
                .border(2.dp, QuestuaGreen.copy(alpha = 0.5f), CircleShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = QuestuaGreen
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Conteúdo Disponível",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = QuestuaGreen
        )
        Spacer(modifier = Modifier.height(16.dp))
        QuestuaButton(
            text = "COMEÇAR AGORA",
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LockedView(state: UnlockPreviewState, onBuyClick: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(QuestuaGold.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, QuestuaGold.copy(alpha = 0.3f), CircleShape)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = QuestuaGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Conteúdo Bloqueado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Adquira acesso premium para desbloquear.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        state.requirement?.let { req ->
            if (req.premiumAccess) {
                PremiumContentSection(
                    products = state.products,
                    userId = state.userId,
                    isProcessing = state.isProcessingPayment,
                    onBuyClick = onBuyClick
                )
            } else {
                Text("Requer Nível: ${req.requiredGamificationLevel}")
            }
        }
    }
}

@Composable
fun PremiumContentSection(
    products: List<Product>,
    userId: String?,
    isProcessing: Boolean,
    onBuyClick: (String) -> Unit
) {
    if (products.isEmpty()) {
        Text("Nenhum produto disponível.", color = MaterialTheme.colorScheme.error)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            products.forEach { product ->
                ProductCard(product, userId, isProcessing, onBuyClick)
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    userId: String?,
    isProcessing: Boolean,
    onBuyClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${product.currency} ${(product.priceCents / 100.0)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = QuestuaGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            QuestuaButton(
                text = if (isProcessing) "..." else "Comprar",
                onClick = { if (userId != null) onBuyClick(product.id) },
                enabled = userId != null && !isProcessing,
                modifier = Modifier.width(130.dp),
                leadingIcon = if (!isProcessing) Icons.Default.ShoppingCart else null
            )
        }
    }
}