package com.questua.app.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar

data class ContentCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGeneralManagementScreen(
    navController: NavController,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogs: () -> Unit,
    onExitAdmin: () -> Unit
) {
    val categories = listOf(
        ContentCategory("quests", "Quests", Icons.Default.Map, 120),
        ContentCategory("dialogues", "Diálogos", Icons.Default.Chat, 450),
        ContentCategory("items", "Itens", Icons.Default.Backpack, 85),
        ContentCategory("npcs", "NPCs", Icons.Default.Person, 32),
        ContentCategory("cities", "Cidades", Icons.Default.LocationCity, 12),
        ContentCategory("achievements", "Conquistas", Icons.Default.EmojiEvents, 50)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Admin") },
                actions = {
                    IconButton(onClick = onNavigateToLogs) {
                        Icon(Icons.Default.History, contentDescription = "Histórico IA")
                    }
                    IconButton(onClick = onExitAdmin) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Visão Geral do Conteúdo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { category ->
                    ContentCategoryCard(category, onClick = { onNavigateToDetail(category.id) })
                }
            }
        }
    }
}

@Composable
fun ContentCategoryCard(category: ContentCategory, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${category.count} itens",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}