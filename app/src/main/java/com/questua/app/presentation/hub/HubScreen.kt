package com.questua.app.presentation.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.UserQuest
import com.questua.app.presentation.hub.components.HeaderStats
import com.questua.app.presentation.hub.components.StreakCard

// Padrão visual dourado
val QuestuaGold = Color(0xFFFFC107)

@Composable
fun HubScreen(
    onNavigateToLanguages: () -> Unit,
    onNavigateToQuest: (String) -> Unit,
    onNavigateToUnlock: (String, String) -> Unit, // id, type (CITY, QUEST, QUEST_POINT)
    onNavigateToContent: (String, String) -> Unit, // id, type
    viewModel: HubViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToLanguages,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = QuestuaGold
            ) {
                Icon(Icons.Default.Language, contentDescription = "Mudar Idioma")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER & STREAK ---
            HeaderStats(
                userName = state.user?.displayName ?: "Explorador",
                userAvatar = state.user?.avatarUrl,
                level = state.activeLanguage?.gamificationLevel ?: 1,
                xp = state.activeLanguage?.xpTotal ?: 0,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            StreakCard(
                streakDays = state.activeLanguage?.streakDays ?: 0,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 1. CONTINUE SUA JORNADA (IN_PROGRESS) ---
            if (state.continueJourneyQuests.isNotEmpty()) {
                SectionTitle(title = "Continue sua Jornada")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.continueJourneyQuests) { userQuest ->
                        ContinueQuestCard(
                            userQuest = userQuest,
                            onClick = { onNavigateToQuest(userQuest.questId) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- 2. NOVIDADES (CIDADES, QUESTS, LOCAIS) ---
            if (state.latestCities.isNotEmpty() || state.latestQuests.isNotEmpty() || state.latestQuestPoints.isNotEmpty()) {
                SectionTitle(title = "Novidades no Questua")

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Cidades
                    if (state.latestCities.isNotEmpty()) {
                        Text("Cidades Recentes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom=8.dp, top = 4.dp))
                        state.latestCities.forEach { item ->
                            NewContentCard(
                                title = item.city.name,
                                imageUrl = item.city.imageUrl,
                                icon = Icons.Default.Map,
                                typeLabel = "CIDADE",
                                isLocked = item.isLocked,
                                onClick = {
                                    if(item.isLocked) onNavigateToUnlock(item.city.id, "CITY")
                                    else onNavigateToContent(item.city.id, "CITY")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Missões (Sem Imagem)
                    if (state.latestQuests.isNotEmpty()) {
                        Text("Missões Recentes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom=8.dp, top = 12.dp))
                        state.latestQuests.forEach { item ->
                            NewContentCard(
                                title = item.quest.title,
                                imageUrl = null, // QUEST NÃO TEM IMAGEM
                                icon = Icons.Default.Assignment, // Ícone de Fallback
                                typeLabel = "MISSÃO",
                                isLocked = item.isLocked,
                                onClick = {
                                    if(item.isLocked) onNavigateToUnlock(item.quest.id, "QUEST")
                                    else onNavigateToContent(item.quest.id, "QUEST")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Locais
                    if (state.latestQuestPoints.isNotEmpty()) {
                        Text("Locais Recentes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom=8.dp, top = 12.dp))
                        state.latestQuestPoints.forEach { item ->
                            NewContentCard(
                                title = item.point.title,
                                imageUrl = item.point.imageUrl,
                                icon = Icons.Default.Place,
                                typeLabel = "LOCAL",
                                isLocked = item.isLocked,
                                onClick = {
                                    if(item.isLocked) onNavigateToUnlock(item.point.id, "QUEST_POINT")
                                    else onNavigateToContent(item.point.id, "QUEST_POINT")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- 3. NOVAS CONQUISTAS (DO SISTEMA) ---
            if (state.latestSystemAchievements.isNotEmpty()) {
                SectionTitle(title = "Novas Conquistas")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.latestSystemAchievements) { achievement ->
                        SystemAchievementCard(achievement)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.width(4.dp).height(24.dp).background(QuestuaGold, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ContinueQuestCard(userQuest: UserQuest, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(280.dp).height(150.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.linearGradient(
                        colors = listOf(QuestuaGold.copy(alpha = 0.05f), MaterialTheme.colorScheme.surface)
                    )
                )
            )

            Column(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = QuestuaGold,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "EM ANDAMENTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Retomar Missão",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { userQuest.percentComplete },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = QuestuaGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(userQuest.percentComplete * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = QuestuaGold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = QuestuaGold,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun NewContentCard(
    title: String,
    imageUrl: String?,
    icon: ImageVector,
    typeLabel: String,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // --- ÁREA DE IMAGEM / ÍCONE ---
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(
                        if (imageUrl == null) QuestuaGold.copy(alpha = 0.1f)
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl.toFullImageUrl())
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback para quando não tem imagem (Missões)
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else QuestuaGold,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Overlay de Lock
                if (isLocked) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White)
                    }
                }
            }

            // --- CONTEÚDO ---
            Column(
                modifier = Modifier.weight(1f).padding(16.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if(isLocked) MaterialTheme.colorScheme.error else QuestuaGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Seta
            Box(
                modifier = Modifier.fillMaxHeight().padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SystemAchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.width(140.dp).height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(QuestuaGold.copy(alpha = 0.1f))
                    .border(1.dp, QuestuaGold.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(achievement.iconUrl?.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    error = rememberVectorPainter(Icons.Default.EmojiEvents)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = achievement.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${achievement.xpReward} XP",
                style = MaterialTheme.typography.labelSmall,
                color = QuestuaGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}