// app/src/main/java/com/questua/app/presentation/admin/monetization/AdminMonetizationScreen.kt
package com.questua.app.presentation.admin.monetization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonetizationScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Monetização & Produtos") }) },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add Product */ }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Produtos Ativos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(3) { index ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        ListItem(
                            headlineContent = { Text("Pacote de Moedas $index") },
                            supportingContent = { Text("R$ 19,90 • 500 Coins") },
                            trailingContent = {
                                IconButton(onClick = { /* Edit Price */ }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                            }
                        )
                    }
                }
            }

            Text("Histórico de Transações", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(5) {
                    ListItem(
                        headlineContent = { Text("Compra #99283") },
                        supportingContent = { Text("User: ana_souza • R$ 19,90") },
                        trailingContent = { Text("Sucesso", color = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
        }
    }
}