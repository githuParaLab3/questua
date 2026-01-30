package com.questua.app.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import com.questua.app.presentation.navigation.Screen

data class ContentCategory(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGeneralManagementScreen(
    navController: NavController,
    onNavigateToLogs: () -> Unit,
    onExitAdmin: () -> Unit,
    viewModel: AdminGeneralManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ATUALIZAÇÃO DINÂMICA AO ENTRAR EM FOCO (RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val categories = remember {
        listOf(
            ContentCategory("languages", "Idiomas", Icons.Default.Translate),
            ContentCategory("cities", "Cidades", Icons.Default.LocationCity),
            ContentCategory("quests", "Quests", Icons.Default.Flag),
            ContentCategory("quest_points", "Quest Points", Icons.Default.Place),
            ContentCategory("dialogues", "Diálogos", Icons.Default.Chat),
            ContentCategory("characters", "Personagens", Icons.Default.Person),
            ContentCategory("achievements", "Conquistas", Icons.Default.EmojiEvents)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel de Controle", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToLogs) {
                        Icon(Icons.Default.AutoGraph, contentDescription = "Histórico IA")
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Gestão de Conteúdo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(categories) { category ->
                    val count = state.counts[category.id] ?: 0
                    ContentCategoryCard(
                        category = category,
                        count = count,
                        onClick = {
                            when (category.id) {
                                "languages" -> navController.navigate(Screen.AdminLanguages.route)
                                "characters" -> navController.navigate(Screen.AdminCharacters.route)
                                "achievements" -> navController.navigate(Screen.AdminAchievements.route)
                                "quest_points" -> navController.navigate(Screen.AdminQuestPoints.route)
                                // As demais rotas serão adicionadas conforme implementação
                                else -> { /* navegação padrão ou erro */ }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCategoryCard(
    category: ContentCategory,
    count: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$count itens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}