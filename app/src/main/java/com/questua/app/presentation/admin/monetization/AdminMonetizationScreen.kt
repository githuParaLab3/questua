package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.TargetType
import com.questua.app.domain.model.Product
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonetizationScreen(
    navController: NavController,
    viewModel: AdminMonetizationViewModel = hiltViewModel()
) {
    val state = viewModel.state

    // CORREÇÃO: Força o recarregamento dos dados sempre que entrar na tela
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    if (state.showProductDialog) {
        ProductFormDialog(
            productToEdit = null,
            onDismiss = { viewModel.closeDialog() },
            onConfirm = { sku, title, desc, price, currency, type, tId ->
                viewModel.saveProduct(sku, title, desc, price, currency, type, tId)
            },
            viewModel = viewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monetização", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchAndFilterSection(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    selectedType = state.activeFilter,
                    onTypeSelected = viewModel::onFilterChange
                )
            }

            item {
                Text(
                    text = "Produtos Ativos (${state.products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.isLoading && state.products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Nenhum produto encontrado.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(state.products) { product ->
                    ProductItem(
                        product = product,
                        onClick = {
                            navController.navigate(Screen.AdminMonetizationDetail.passId(product.id))
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Histórico Recente de Transações",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.transactions.isEmpty()) {
                item {
                    Text(
                        "Nenhuma transação registrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(state.transactions) { transaction ->
                    ListItem(
                        headlineContent = { Text("ID: ${transaction.stripePaymentIntentId.take(8)}...") },
                        supportingContent = { Text("${transaction.currency} ${(transaction.amountCents / 100.0)} • ${transaction.status}") },
                        leadingContent = { Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.secondary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SearchAndFilterSection(query: String, onQueryChange: (String) -> Unit, selectedType: TargetType?, onTypeSelected: (TargetType?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por título ou SKU...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, null) } },
            singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedType == null, onClick = { onTypeSelected(null) }, label = { Text("Todos") })
            TargetType.values().forEach { type ->
                FilterChip(selected = selectedType == type, onClick = { onTypeSelected(type) }, label = { Text(type.name) })
            }
        }
    }
}

@Composable
fun ProductFormDialog(productToEdit: Product? = null, onDismiss: () -> Unit, onConfirm: (String, String, String, Int, String, TargetType, String) -> Unit, viewModel: AdminMonetizationViewModel) {
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var title by remember { mutableStateOf(productToEdit?.title ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var priceStr by remember { mutableStateOf(productToEdit?.priceCents?.toString() ?: "") }
    var currency by remember { mutableStateOf(productToEdit?.currency ?: "BRL") }
    var targetType by remember { mutableStateOf(productToEdit?.targetType ?: TargetType.CITY) }
    var targetId by remember { mutableStateOf(productToEdit?.targetId ?: "") }
    var targetNameDisplay by remember { mutableStateOf(viewModel.state.selectedTargetName ?: "") }

    LaunchedEffect(viewModel.state.selectedTargetName) {
        if (viewModel.state.selectedTargetName != null) targetNameDisplay = viewModel.state.selectedTargetName!!
        else if (productToEdit != null && targetNameDisplay.isEmpty()) targetNameDisplay = "Item Atual (ID: ${productToEdit.targetId.take(8)}...)"
    }

    if (viewModel.state.showTargetSelector) {
        TargetSelectionDialog(items = viewModel.state.selectorItems, onDismiss = { viewModel.closeTargetSelector() }, onSelect = { item ->
            targetId = item.id
            targetNameDisplay = item.name
            viewModel.closeTargetSelector()
        })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productToEdit == null) "Novo Produto" else "Editar Produto") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuestuaTextField(value = sku, onValueChange = { sku = it }, label = "SKU", modifier = Modifier.fillMaxWidth())
                QuestuaTextField(value = title, onValueChange = { title = it }, label = "Título", modifier = Modifier.fillMaxWidth())
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição", modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuestuaTextField(value = priceStr, onValueChange = { if (it.all { c -> c.isDigit() }) priceStr = it }, label = "Preço (centavos)", modifier = Modifier.weight(1f))
                    QuestuaTextField(value = currency, onValueChange = { currency = it.uppercase() }, label = "Moeda", modifier = Modifier.width(100.dp))
                }
                Text("Conteúdo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TargetType.values().forEach { type ->
                        FilterChip(selected = targetType == type, onClick = { if (type != targetType) { targetType = type; targetId = ""; targetNameDisplay = "" } }, label = { Text(type.name) })
                    }
                }
                OutlinedCard(onClick = { viewModel.openTargetSelector(targetType) }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = if (targetId.isEmpty()) "Selecionar..." else targetNameDisplay, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Search, null)
                    }
                }
            }
        },
        confirmButton = {
            QuestuaButton(text = if (productToEdit == null) "Criar" else "Salvar", enabled = targetId.isNotEmpty() && sku.isNotBlank() && title.isNotBlank(), onClick = {
                onConfirm(sku, title, description, priceStr.toIntOrNull() ?: 0, currency, targetType, targetId)
            }, modifier = Modifier.width(120.dp))
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun TargetSelectionDialog(items: List<SelectorItem>, onDismiss: () -> Unit, onSelect: (SelectorItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(items, query) { items.filter { it.name.contains(query, true) || it.id.contains(query) } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Selecione o Item") }, text = {
        Column(modifier = Modifier.height(400.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Buscar...") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(filtered) { item ->
                    ListItem(headlineContent = { Text(item.name) }, supportingContent = { Text(item.detail) }, modifier = Modifier.clickable { onSelect(item) }.fillMaxWidth())
                    HorizontalDivider()
                }
            }
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}