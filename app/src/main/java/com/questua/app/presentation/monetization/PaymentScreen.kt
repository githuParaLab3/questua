package com.questua.app.presentation.monetization

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

@Composable
fun PaymentScreen(
    productId: String,
    userId: String,
    onPaymentSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    val paymentSheet = rememberPaymentSheet(paymentResultCallback = viewModel::onPaymentResult)

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(state.clientSecret) {
        state.clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = secret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Questua App"
                )
            )
            viewModel.resetPaymentState()
        }
    }

    LaunchedEffect(state.paymentStatus) {
        when (state.paymentStatus) {
            PaymentStatus.SUCCESS -> {
                Toast.makeText(context, "Pagamento realizado com sucesso!", Toast.LENGTH_LONG).show()
                onPaymentSuccess()
            }
            PaymentStatus.FAILED -> {
                Toast.makeText(context, "Erro: ${state.paymentError}", Toast.LENGTH_LONG).show()
            }
            PaymentStatus.CANCELED -> {
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                LoadingSpinner()
            } else if (state.product != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = state.product.title,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = state.product.description ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Preço: ${formatCurrency(state.product.priceCents, state.product.currency)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    QuestuaButton(
                        text = if (state.isProcessingPayment) "Processando..." else "Comprar Agora",
                        onClick = {
                            viewModel.initiatePayment(userId)
                        },
                        enabled = !state.isProcessingPayment
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else if (state.error != null) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun formatCurrency(amountCents: Int, currency: String): String {
    val amount = amountCents / 100.0
    return "$currency $amount"
}