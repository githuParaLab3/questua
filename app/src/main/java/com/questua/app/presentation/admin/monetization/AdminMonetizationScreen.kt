package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.TargetType
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonetizationScreen(
    navController: NavController,
    viewModel: AdminMonetizationViewModel = hiltViewModel()
) {
    val state = viewModel.state

    if (state.showCreateModal) {
        CreateProductDialog(
            onDismiss = { viewModel.toggleCreateModal(false) },
            onConfirm = { sku, title, desc, price, type, tId ->
                viewModel.createProduct(sku, title, desc, price, type, tId)
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monetização & Produtos") }) },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleCreateModal(true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Seção de Produtos
            Text(
                "Produtos Ativos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading && state.products.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.products) { product ->
                        ProductItem(
                            title = product.title,
                            sku = product.sku,
                            price = "${product.currency} ${(product.priceCents / 100.0)}",
                            type = product.targetType.name,
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }

            // Seção de Transações
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Histórico de Transações",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.transactions) { transaction ->
                    ListItem(
                        headlineContent = { Text("ID: ${transaction.stripePaymentIntentId.take(8)}...") },
                        supportingContent = { Text("R$ ${(transaction.amountCents / 100.0)} - ${transaction.status}") },
                        trailingContent = {
                            Text(
                                transaction.currency,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    title: String,
    sku: String,
    price: String,
    type: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = "SKU: $sku", style = MaterialTheme.typography.bodySmall)
                Text(text = "$type • $price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CreateProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, TargetType, String) -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(TargetType.CITY) }
    var targetId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Produto") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuestuaTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = "SKU (ex: coin_pack_1)",
                    modifier = Modifier.fillMaxWidth()
                )
                QuestuaTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título",
                    modifier = Modifier.fillMaxWidth()
                )
                QuestuaTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descrição",
                    modifier = Modifier.fillMaxWidth()
                )
                QuestuaTextField(
                    value = priceStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) priceStr = it },
                    label = "Preço em Centavos (ex: 1990)",
                    modifier = Modifier.fillMaxWidth()
                )
                QuestuaTextField(
                    value = targetId,
                    onValueChange = { targetId = it },
                    label = "ID do Alvo (UUID)",
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de Alvo", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown simplificado ou Chips para seleção
                    TargetType.values().forEach { type ->
                        FilterChip(
                            selected = targetType == type,
                            onClick = { targetType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            QuestuaButton(
                text = "Criar",
                onClick = {
                    val price = priceStr.toIntOrNull() ?: 0
                    if (sku.isNotBlank() && title.isNotBlank()) {
                        onConfirm(sku, title, description, price, targetType, targetId)
                    }
                },
                modifier = Modifier.width(100.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}