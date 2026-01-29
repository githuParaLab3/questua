package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.domain.enums.TransactionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductDetailScreen(
    navController: NavController,
    viewModel: AdminProductDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showEditDialog && state.product != null) {
        ProductFormDialog(
            productToEdit = state.product,
            onDismiss = { showEditDialog = false },
            onConfirm = { sku, title, desc, price, curr, type, tId ->
                viewModel.saveProduct(sku, title, desc, price, curr, type, tId)
                showEditDialog = false
            },
            viewModel = hiltViewModel<AdminMonetizationViewModel>()
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Produto") },
            text = { Text("Tem certeza que deseja excluir '${state.product?.title}'? Esta ação é irreversível.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct { navController.popBackStack() }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Produto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (state.product != null) {
            val product = state.product
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de Informações
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(product.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("SKU: ${product.sku}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(product.description ?: "Sem descrição.", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Card de Preço
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Preço", fontWeight = FontWeight.SemiBold)
                        Text("${product.currency} ${String.format("%.2f", product.priceCents / 100.0)}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                    }
                }

                // Card de Vínculo
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Conteúdo Vinculado", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("Tipo: ${product.targetType.name}")
                        Text("ID Alvo: ${product.targetId}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Botões de Ação
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuestuaButton(text = "Editar Produto", onClick = { showEditDialog = true }, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Excluir")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // SEÇÃO DE TRANSAÇÕES
                Text("Transações deste Produto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                // Busca
                OutlinedTextField(
                    value = state.transactionQuery,
                    onValueChange = viewModel::onTransactionQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar ID da transação...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Filtros por Status (Chips)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedStatus == null,
                        onClick = { viewModel.onStatusSelected(null) },
                        label = { Text("Todos") }
                    )
                    TransactionStatus.values().forEach { status ->
                        FilterChip(
                            selected = state.selectedStatus == status,
                            onClick = { viewModel.onStatusSelected(status) },
                            label = { Text(status.name) }
                        )
                    }
                }

                if (state.isTransactionsLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (state.filteredTransactions.isEmpty()) {
                    Text("Nenhuma transação encontrada.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    state.filteredTransactions.forEach { transaction ->
                        ListItem(
                            headlineContent = { Text("ID: ${transaction.stripePaymentIntentId.take(12)}...") },
                            supportingContent = {
                                Text("Status: ${transaction.status.name}", color = when(transaction.status) {
                                    TransactionStatus.SUCCEEDED -> Color(0xFF2E7D32)
                                    TransactionStatus.FAILED -> Color.Red
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                })
                            },
                            trailingContent = {
                                Text("${transaction.currency} ${String.format("%.2f", transaction.amountCents / 100.0)}", fontWeight = FontWeight.Bold)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}