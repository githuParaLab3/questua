package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.questua.app.domain.model.Product
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonetizationScreen(
    navController: NavController,
    viewModel: AdminMonetizationViewModel = hiltViewModel()
) {
    val state = viewModel.state

    // Se o modal estiver visível, renderiza o diálogo de produto (criação ou edição)
    if (state.showProductDialog) {
        ProductFormDialog(
            productToEdit = state.productToEdit,
            onDismiss = { viewModel.closeDialog() },
            onConfirm = { sku, title, desc, price, currency, type, tId ->
                viewModel.saveProduct(sku, title, desc, price, currency, type, tId)
            },
            viewModel = viewModel
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monetização & Produtos") }) },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
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
                            product = product,
                            onEdit = { viewModel.openEditDialog(product) }, // <--- Ação de editar
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
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
    product: Product,
    onEdit: () -> Unit,
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
                Text(text = product.title, style = MaterialTheme.typography.titleSmall)
                Text(text = "SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${product.targetType.name} • ${product.currency} ${(product.priceCents / 100.0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Botões de Ação
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    productToEdit: Product? = null, // <--- Dados para preencher se for edição
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, TargetType, String) -> Unit,
    viewModel: AdminMonetizationViewModel
) {
    // Inicializa estados com valores do produto se estiver editando, ou vazio
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var title by remember { mutableStateOf(productToEdit?.title ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var priceStr by remember { mutableStateOf(productToEdit?.priceCents?.toString() ?: "") }
    var currency by remember { mutableStateOf(productToEdit?.currency ?: "BRL") }
    var targetType by remember { mutableStateOf(productToEdit?.targetType ?: TargetType.CITY) }

    var targetId by remember { mutableStateOf(productToEdit?.targetId ?: "") }
    var targetNameDisplay by remember { mutableStateOf(viewModel.state.selectedTargetName ?: "") }

    // Atualiza nome do alvo se a VM tiver carregado algo novo no seletor
    LaunchedEffect(viewModel.state.selectedTargetName) {
        if (viewModel.state.selectedTargetName != null) {
            targetNameDisplay = viewModel.state.selectedTargetName!!
        } else if (productToEdit != null && targetNameDisplay.isEmpty()) {
            // Fallback inicial na edição
            targetNameDisplay = "ID: ${productToEdit.targetId}"
        }
    }

    val state = viewModel.state

    if (state.showTargetSelector) {
        TargetSelectionDialog(
            items = state.selectorItems,
            onDismiss = { viewModel.closeTargetSelector() },
            onSelect = { item ->
                targetId = item.id
                targetNameDisplay = item.name
                viewModel.closeTargetSelector()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productToEdit == null) "Novo Produto" else "Editar Produto") },
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuestuaTextField(
                        value = priceStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) priceStr = it },
                        label = "Preço (Centavos)",
                        modifier = Modifier.weight(1f)
                    )
                    QuestuaTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = "Moeda",
                        modifier = Modifier.width(100.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BRL", "USD", "EUR").forEach { curr ->
                        FilterChip(
                            selected = currency == curr,
                            onClick = { currency = curr },
                            label = { Text(curr) }
                        )
                    }
                }

                HorizontalDivider()
                Text("Vincular Conteúdo", style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetType.values().forEach { type ->
                        FilterChip(
                            selected = targetType == type,
                            onClick = {
                                targetType = type
                                // Ao mudar tipo na edição, reseta o ID para forçar nova escolha
                                if (type != productToEdit?.targetType) {
                                    targetId = ""
                                    targetNameDisplay = ""
                                }
                            },
                            label = { Text(type.name) }
                        )
                    }
                }

                OutlinedCard(
                    onClick = { viewModel.openTargetSelector(targetType) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (targetId.isEmpty()) "Selecione ${targetType.name}..." else targetNameDisplay,
                                style = if (targetId.isEmpty()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                                color = if (targetId.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                            if (targetId.isNotEmpty()) {
                                Text(
                                    text = "ID: ${targetId.take(8)}...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            QuestuaButton(
                text = if (productToEdit == null) "Criar" else "Salvar",
                enabled = targetId.isNotEmpty() && sku.isNotBlank() && title.isNotBlank() && currency.isNotBlank(),
                onClick = {
                    val price = priceStr.toIntOrNull() ?: 0
                    onConfirm(sku, title, description, price, currency, targetType, targetId)
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

@Composable
fun TargetSelectionDialog(
    items: List<SelectorItem>,
    onDismiss: () -> Unit,
    onSelect: (SelectorItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = items.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione o Item") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nome...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(filteredItems) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = { Text("${item.detail} • ID: ${item.id.take(6)}...") },
                            modifier = Modifier
                                .clickable { onSelect(item) }
                                .fillMaxWidth()
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}