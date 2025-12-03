package com.questua.app.presentation.admin.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLogsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Geração IA") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(15) { index ->
                ListItem(
                    headlineContent = { Text("Geração de Quest #$index") },
                    supportingContent = { Text("Prompt: 'Uma aventura medieval...' • Status: Sucesso") },
                    trailingContent = {
                        Text("02/12 14:30", style = MaterialTheme.typography.bodySmall)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}