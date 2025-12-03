package com.questua.app.presentation.admin.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    navController: NavController,
    contentType: String,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val title = when(contentType) {
        "quests" -> "Gerenciar Quests"
        "dialogues" -> "Gerenciar Diálogos"
        else -> "Gerenciar Conteúdo"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { /* Trigger AI Generation */ },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Gerar com IA")
                }
                FloatingActionButton(onClick = { /* Create Manual */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Criar Novo")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(10) { index ->
                    ListItem(
                        headlineContent = { Text("Item de Conteúdo #$index") },
                        supportingContent = { Text("ID: item_00$index • Status: Ativo") },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.clickable { /* Edit */ }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}