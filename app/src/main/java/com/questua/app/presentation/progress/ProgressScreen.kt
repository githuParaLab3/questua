package com.questua.app.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.domain.model.UserAchievement
import com.questua.app.domain.model.UserLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seu Progresso", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                LoadingSpinner()
            } else {
                val userLang = state.userLanguage
                val xp = if (state.filter == ProgressFilter.GLOBAL) 4500 else userLang?.xpTotal ?: 0
                val level = if (state.filter == ProgressFilter.GLOBAL) 25 else userLang?.gamificationLevel ?: 1
                val streak = userLang?.streakDays ?: 0
                val questsCompleted = if (state.filter == ProgressFilter.GLOBAL) 120 else 12

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // 1. Filtro
                        ProgressFilterSegmentedButton(
                            currentFilter = state.filter,
                            onFilterChange = viewModel::setFilter
                        )
                    }

                    item {
                        // 2. Título e Subtítulo
                        val title = when (state.filter) {
                            ProgressFilter.GLOBAL -> "Estatísticas Globais"
                            ProgressFilter.ACTIVE_LANGUAGE -> "Estatísticas do Idioma Ativo"
                        }
                        val subtitle = when (state.filter) {
                            ProgressFilter.GLOBAL -> "Total acumulado em todos os idiomas."
                            ProgressFilter.ACTIVE_LANGUAGE -> "Progresso em ${state.languageDetails?.name ?: "Carregando..."}"
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 3. Cartões de Estatísticas
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.Star,
                                title = "Nível",
                                value = level.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Bolt,
                                title = "XP Total",
                                value = xp.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.LocalFireDepartment,
                                title = "Ofensiva",
                                value = "${streak} dias",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.TaskAlt,
                                title = "Missões",
                                value = "${questsCompleted} completas",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Gráficos (Mock)
                    item {
                        Text(
                            text = "Histórico de XP (Últimas 4 Semanas)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Gráfico de XP (Implementação UI)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // 5. Conquistas
                    item {
                        Text(
                            text = "Conquistas Desbloqueadas (${state.achievements.size})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                        )
                    }

                    items(state.achievements) { achievement ->
                        AchievementItem(achievement = achievement)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            state.error?.let {
                AlertDialog(
                    onDismissRequest = { /* Não dismissível no erro de carregamento */ },
                    title = { Text("Erro de Carregamento") },
                    text = { Text(it ?: "Não foi possível carregar os dados de progresso.") },
                    confirmButton = {
                        TextButton(onClick = { state.userLanguage?.userId?.let { viewModel.loadProgressData(it) } }) {
                            Text("Tentar Novamente")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AchievementItem(achievement: ProgressAchievementUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Conquistado em: ${achievement.userAchievement.awardedAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.Check, contentDescription = "Conquistado", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressFilterSegmentedButton(
    currentFilter: ProgressFilter,
    onFilterChange: (ProgressFilter) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ProgressFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ProgressFilter.entries.size),
                onClick = { onFilterChange(filter) },
                selected = filter == currentFilter
            ) {
                Text(if (filter == ProgressFilter.GLOBAL) "Global" else "Idioma Ativo")
            }
        }
    }
}