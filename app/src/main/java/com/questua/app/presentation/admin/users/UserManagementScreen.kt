package com.questua.app.presentation.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gerenciamento de Usuários") }) },
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Filtros e Busca
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar usuário...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                IconButton(onClick = { /* Open Filters */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabela de Usuários
            LazyColumn {
                items(10) { index ->
                    UserListItem(index)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun UserListItem(index: Int) {
    ListItem(
        headlineContent = { Text("Usuário Teste $index") },
        supportingContent = { Text("user$index@email.com • Role: USER") },
        trailingContent = {
            Row {
                IconButton(onClick = { /* Edit */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { /* Delete/Ban */ }) {
                    Icon(Icons.Default.Block, contentDescription = "Bloquear", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}