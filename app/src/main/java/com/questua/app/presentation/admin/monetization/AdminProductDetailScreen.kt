package com.questua.app.presentation.admin.monetization

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
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(product.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("SKU: ${product.sku}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(product.description ?: "Sem descrição.", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preço", fontWeight = FontWeight.SemiBold)
                        Text("${product.currency} ${String.format("%.2f", product.priceCents / 100.0)}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                    }
                }

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Conteúdo Vinculado", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("Tipo: ${product.targetType.name}")
                        Text("ID Alvo: ${product.targetId}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Seção de Ações abaixo dos detalhes
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuestuaButton(
                        text = "Editar Produto",
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Excluir Produto")
                    }
                }
            }
        }
    }
}