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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.LoadingSpinner

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
            if (state.isLoading && state.userLanguage == null) {
                LoadingSpinner()
            } else {
                val userLang = state.userLanguage
                val isGlobal = state.filter == ProgressFilter.GLOBAL

                // Dados calculados
                val xp = if (isGlobal) state.globalXp else userLang?.xpTotal ?: 0
                val level = if (isGlobal) state.globalLevel else userLang?.gamificationLevel ?: 1

                val streakValue = if (isGlobal) state.globalStreak else userLang?.streakDays ?: 0
                val streakTitle = if (isGlobal) "Melhor Ofensiva" else "Ofensiva"
                val streakSubtitle = if (isGlobal && state.bestStreakLanguageName != null)
                    "em ${state.bestStreakLanguageName}"
                else "dias seguidos"

                val questsCompleted = if (isGlobal) state.globalQuestsCount else state.activeQuestsCount
                val questPointsUnlocked = if (isGlobal) state.globalQuestPointsCount else state.activeQuestPointsCount
                val citiesUnlocked = if (isGlobal) state.globalCitiesCount else state.activeCitiesCount

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ProgressFilterSegmentedButton(
                            currentFilter = state.filter,
                            onFilterChange = viewModel::setFilter
                        )
                    }

                    item {
                        val title = if (isGlobal) "Estatísticas Globais" else "Estatísticas do Idioma Ativo"
                        val subtitle = if (isGlobal)
                            "Total acumulado em todas as suas jornadas."
                        else
                            "Progresso em ${state.languageDetails?.name ?: "seu idioma atual"}"

                        Column {
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
                    }

                    // Grid de Estatísticas (3 Linhas x 2 Colunas)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Linha 1: Nível e XP
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

                            // Linha 2: Ofensiva e Cidades
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.LocalFireDepartment,
                                    title = streakTitle,
                                    value = "$streakValue dias",
                                    subtitle = streakSubtitle,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.LocationCity,
                                    title = "Cidades",
                                    value = "$citiesUnlocked",
                                    subtitle = "Desbloqueadas",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Linha 3: Missões e Pontos de Quest
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.TaskAlt,
                                    title = "Missões",
                                    value = "$questsCompleted",
                                    subtitle = "Completas",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.Place,
                                    title = "Pontos",
                                    value = "$questPointsUnlocked",
                                    subtitle = "Visitados",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        val achievementsTitle = if (isGlobal) "Todas as Conquistas" else "Conquistas neste Idioma"
                        Text(
                            text = "$achievementsTitle (${state.achievements.size})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                        )
                    }

                    if (state.achievements.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhuma conquista encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(state.achievements) { achievement ->
                            AchievementItem(achievement = achievement)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            state.error?.let {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Erro de Carregamento") },
                    text = { Text(it) },
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
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
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

                // Formatação manual da data: YYYY-MM-DD -> DD-MM-YYYY
                val rawDate = achievement.userAchievement.awardedAt.take(10)
                val formattedDate = try {
                    if (rawDate.contains("-") && rawDate.length == 10) {
                        val parts = rawDate.split("-")
                        "${parts[2]}-${parts[1]}-${parts[0]}" // DD-MM-YYYY
                    } else {
                        rawDate
                    }
                } catch (e: Exception) {
                    rawDate
                }

                Text(
                    text = "Conquistado em: $formattedDate",
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