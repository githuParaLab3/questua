package com.questua.app.presentation.progress

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.managers.AchievementMonitor

val QuestuaGold = Color(0xFFFFC107)
val QuestuaPurple = Color(0xFF6200EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    achievementMonitor: AchievementMonitor
) {
    val state by viewModel.state.collectAsState()
    val unseenIds by achievementMonitor.unseenAchievementIds.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.userId?.let { viewModel.loadProgressData(it) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.filter, state.achievements) {
        if (state.achievements.isNotEmpty()) {
            val visibleNewIds = state.achievements
                .map { it.userAchievement.achievementId }
                .filter { unseenIds.contains(it) }

            if (visibleNewIds.isNotEmpty()) {
                achievementMonitor.markSeenByContext(visibleNewIds)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Seu Progresso", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Brush.verticalGradient(listOf(QuestuaGold.copy(alpha = 0.15f), MaterialTheme.colorScheme.background))))

            if (state.isLoading && state.userId == null) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                val isGlobal = state.filter == ProgressFilter.GLOBAL
                val xp = if (isGlobal) state.globalXp else state.userLanguage?.xpTotal ?: 0
                val level = if (isGlobal) state.globalLevel else state.userLanguage?.gamificationLevel ?: 1
                val streakValue = if (isGlobal) state.globalStreak else state.userLanguage?.streakDays ?: 0

                LazyColumn(
                    contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { ProgressFilterSegmentedButton(state.filter, viewModel::setFilter) }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatCard(Icons.Default.Star, "Nível", level.toString(), modifier = Modifier.weight(1f), accentColor = QuestuaGold)
                                StatCard(Icons.Default.Bolt, "XP Total", xp.toString(), modifier = Modifier.weight(1f), accentColor = QuestuaGold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatCard(Icons.Default.LocalFireDepartment, "Ofensiva", "$streakValue", "dias", Modifier.weight(1f), Color(0xFFFF5722))
                                StatCard(Icons.Default.LocationCity, "Cidades", "${if (isGlobal) state.globalCitiesCount else state.activeCitiesCount}", "Vilas", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item { ActivityGraphCard(state.achievementsThisWeek, state.achievementsThisMonth) }

                    item { Text("Conquistas (${state.achievements.size})", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) }

                    items(state.achievements, key = { it.userAchievement.id }) { achievement ->
                        val isNew = unseenIds.contains(achievement.userAchievement.achievementId)
                        AchievementItem(achievement = achievement, isHighlighted = isNew)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: ProgressAchievementUiModel, isHighlighted: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")
    val borderColor by infiniteTransition.animateColor(
        initialValue = if (isHighlighted) QuestuaGold else Color.Transparent,
        targetValue = if (isHighlighted) QuestuaGold.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "borderColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isHighlighted) 8.dp else 2.dp),
        border = BorderStroke(if (isHighlighted) 2.dp else 1.dp, if (isHighlighted) borderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(QuestuaGold.copy(alpha = 0.1f)).border(1.dp, QuestuaGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                if (achievement.iconUrl != null) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(achievement.iconUrl.toFullImageUrl()).build(), contentDescription = null, modifier = Modifier.size(32.dp))
                } else { Icon(Icons.Default.EmojiEvents, null, tint = QuestuaGold, modifier = Modifier.size(28.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(achievement.name, fontWeight = FontWeight.Bold)
                    if (isHighlighted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = QuestuaGold, shape = RoundedCornerShape(4.dp)) {
                            Text("NOVO", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Text(achievement.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Icon(Icons.Default.CheckCircle, null, tint = if (isHighlighted) QuestuaGold else Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ActivityGraphCard(weekCount: Int, monthCount: Int) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ActivityBar(label = "Esta Semana", count = weekCount, max = 5, color = QuestuaGold)
                ActivityBar(label = "Este Mês", count = monthCount, max = 15, color = QuestuaPurple)
            }
        }
    }
}

@Composable
fun RowScope.ActivityBar(label: String, count: Int, max: Int, color: Color) {
    val progress = (count.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("$count", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = color, trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String, subtitle: String? = null, modifier: Modifier = Modifier, accentColor: Color = MaterialTheme.colorScheme.primary) {
    Card(modifier = modifier.height(140.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(accentColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) { Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressFilterSegmentedButton(currentFilter: ProgressFilter, onFilterChange: (ProgressFilter) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ProgressFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(shape = SegmentedButtonDefaults.itemShape(index = index, count = ProgressFilter.entries.size), onClick = { onFilterChange(filter) }, selected = filter == currentFilter, colors = SegmentedButtonDefaults.colors(activeContainerColor = QuestuaGold.copy(alpha = 0.2f), activeContentColor = MaterialTheme.colorScheme.onSurface, activeBorderColor = QuestuaGold)) {
                Text(text = if (filter == ProgressFilter.GLOBAL) "Global" else "Idioma Ativo", fontWeight = if (filter == currentFilter) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}