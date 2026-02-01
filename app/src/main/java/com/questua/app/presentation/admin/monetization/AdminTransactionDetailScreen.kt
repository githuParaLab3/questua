package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.TransactionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTransactionDetailScreen(
    onBack: () -> Unit,
    viewModel: AdminTransactionDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Transação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                is Resource.Error -> Text(state.message ?: "Erro", color = Color.Red)
                is Resource.Success -> {
                    val transaction = state.data!!
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(transaction.statusTransaction)

                        InfoSection("Informações Gerais") {
                            InfoRow("ID da Transação", transaction.id)
                            InfoRow("Data de Criação", transaction.createdAt)
                            transaction.completedAt?.let { InfoRow("Concluída em", it) }
                        }

                        InfoSection("Valores") {
                            val amountFormatted = "R$ ${transaction.amountCents / 100.0}"
                            InfoRow("Valor Total", "$amountFormatted ${transaction.currency}")
                        }

                        InfoSection("Relacionamentos") {
                            InfoRow("ID do Usuário", transaction.userId)
                            InfoRow("ID do Produto", transaction.productId)
                        }

                        InfoSection("Stripe") {
                            InfoRow("Payment Intent ID", transaction.stripePaymentIntentId)
                            transaction.stripeChargeId?.let { InfoRow("Charge ID", it) }
                            transaction.receiptUrl?.let { InfoRow("URL do Recibo", it, isLink = true) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(status: TransactionStatus) {
    val color = when (status) {
        TransactionStatus.SUCCEEDED -> Color(0xFF4CAF50)
        TransactionStatus.FAILED -> Color.Red
        TransactionStatus.PENDING -> Color(0xFFFFC107)
        else -> Color.Gray
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Status: ${status.name}",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, isLink: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLink) MaterialTheme.colorScheme.secondary else Color.Unspecified
        )
    }
}