package com.questua.app.presentation.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.LoadingSpinner

// Cor Dourada Padrão
val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Seu Progresso",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente de Fundo (Estilo Padrão)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                QuestuaGold.copy(alpha = 0.15f), // Ajustado para 0.15f (Padrão Admin)
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            if (state.isLoading && state.userLanguage == null) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                val userLang = state.userLanguage
                val isGlobal = state.filter == ProgressFilter.GLOBAL

                // Dados calculados
                val xp = if (isGlobal) state.globalXp else userLang?.xpTotal ?: 0
                val level = if (isGlobal) state.globalLevel else userLang?.gamificationLevel ?: 1

                val streakValue = if (isGlobal) state.globalStreak else userLang?.streakDays ?: 0
                val streakTitle = if (isGlobal) "Melhor Ofensiva" else "Ofensiva Atual"
                val streakSubtitle = if (isGlobal && state.bestStreakLanguageName != null)
                    "em ${state.bestStreakLanguageName}"
                else "dias seguidos"

                val questsCompleted = if (isGlobal) state.globalQuestsCount else state.activeQuestsCount
                val questPointsUnlocked = if (isGlobal) state.globalQuestPointsCount else state.activeQuestPointsCount
                val citiesUnlocked = if (isGlobal) state.globalCitiesCount else state.activeCitiesCount

                LazyColumn(
                    contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- Filtro ---
                    item {
                        ProgressFilterSegmentedButton(
                            currentFilter = state.filter,
                            onFilterChange = viewModel::setFilter
                        )
                    }

                    // --- Título da Seção ---
                    item {
                        val title = if (isGlobal) "Estatísticas Globais" else "Em ${state.languageDetails?.name ?: "Andamento"}"
                        val subtitle = if (isGlobal)
                            "Resumo de todas as suas jornadas."
                        else
                            "Progresso focado neste idioma."

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // --- Grid de Estatísticas ---
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Linha 1: Nível e XP (Destaque)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.Star,
                                    title = "Nível",
                                    value = level.toString(),
                                    modifier = Modifier.weight(1f),
                                    accentColor = QuestuaGold
                                )
                                StatCard(
                                    icon = Icons.Default.Bolt,
                                    title = "XP Total",
                                    value = xp.toString(),
                                    modifier = Modifier.weight(1f),
                                    accentColor = QuestuaGold
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
                                    value = "$streakValue",
                                    subtitle = streakSubtitle,
                                    modifier = Modifier.weight(1f),
                                    accentColor = Color(0xFFFF5722) // Laranja
                                )
                                StatCard(
                                    icon = Icons.Default.LocationCity,
                                    title = "Cidades",
                                    value = "$citiesUnlocked",
                                    subtitle = "Desbloqueadas",
                                    modifier = Modifier.weight(1f),
                                    accentColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Linha 3: Missões e Pontos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.TaskAlt,
                                    title = "Missões",
                                    value = "$questsCompleted",
                                    subtitle = "Completas",
                                    modifier = Modifier.weight(1f),
                                    accentColor = MaterialTheme.colorScheme.secondary
                                )
                                StatCard(
                                    icon = Icons.Default.Place,
                                    title = "Pontos",
                                    value = "$questPointsUnlocked",
                                    subtitle = "Visitados",
                                    modifier = Modifier.weight(1f),
                                    accentColor = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    // --- Conquistas ---
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        val achievementsTitle = if (isGlobal) "Todas as Conquistas" else "Conquistas Locais"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .background(QuestuaGold, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$achievementsTitle (${state.achievements.size})",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    if (state.achievements.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Nenhuma conquista desbloqueada ainda.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = { Text("Erro de Carregamento", fontWeight = FontWeight.Bold) },
                    text = { Text(it) },
                    confirmButton = {
                        Button(
                            onClick = { state.userLanguage?.userId?.let { viewModel.loadProgressData(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = QuestuaGold, contentColor = Color.Black)
                        ) {
                            Text("Tentar Novamente", fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
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
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier.height(140.dp), // Altura levemente aumentada
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Icone e Título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Value
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: ProgressAchievementUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone da Conquista
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(QuestuaGold.copy(alpha = 0.1f))
                    .border(1.dp, QuestuaGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = QuestuaGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val rawDate = achievement.userAchievement.awardedAt.take(10)
                val formattedDate = try {
                    if (rawDate.contains("-") && rawDate.length == 10) {
                        val parts = rawDate.split("-")
                        "${parts[2]}/${parts[1]}/${parts[0]}" // DD/MM/YYYY
                    } else {
                        rawDate
                    }
                } catch (e: Exception) {
                    rawDate
                }

                Text(
                    text = "Desbloqueado em $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Checkmark
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Conquistado",
                tint = Color(0xFF4CAF50), // Verde Sucesso
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressFilterSegmentedButton(
    currentFilter: ProgressFilter,
    onFilterChange: (ProgressFilter) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        ProgressFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ProgressFilter.entries.size),
                onClick = { onFilterChange(filter) },
                selected = filter == currentFilter,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = QuestuaGold.copy(alpha = 0.2f),
                    activeContentColor = MaterialTheme.colorScheme.onSurface,
                    activeBorderColor = QuestuaGold,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (filter == ProgressFilter.GLOBAL) "Global" else "Idioma Ativo",
                    fontWeight = if (filter == currentFilter) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}