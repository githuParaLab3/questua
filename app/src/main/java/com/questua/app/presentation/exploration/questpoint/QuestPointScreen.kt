package com.questua.app.presentation.exploration.questpoint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner

val QuestuaGold = Color(0xFFFFC107)

@Composable
fun QuestPointScreen(
    onNavigateBack: () -> Unit,
    onQuestClick: (String) -> Unit,
    onNavigateToUnlock: (String, String) -> Unit,
    viewModel: QuestPointViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // --- HEADER IMERSIVO ---
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp) // Aumentado para 380dp para dar mais área visual
                        ) {
                            // Imagem de Fundo
                            state.questPoint?.let { point ->
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(point.imageUrl?.toFullImageUrl())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = point.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )

                            // Gradiente Overlay (Mais forte no fundo para contraste)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Black.copy(alpha = 0.95f)
                                            ),
                                            startY = 150f
                                        )
                                    )
                            )

                            // Informações do Ponto
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 24.dp)
                                    // LIMITADOR DE ALTURA:
                                    // Garante que o texto nunca suba além de 280dp (deixando 100dp livres no topo para o botão voltar)
                                    .heightIn(max = 280.dp)
                            ) {
                                // Badge
                                Surface(
                                    color = QuestuaGold,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "LOCAL DE EXPLORAÇÃO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = state.questPoint?.title ?: "Carregando...",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            blurRadius = 12f
                                        )
                                    ),
                                    color = Color.White,
                                    maxLines = 3, // Limita linhas para não quebrar o layout
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = state.questPoint?.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Barra de Progresso
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LinearProgressIndicator(
                                        progress = { state.totalProgressPercent },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = QuestuaGold,
                                        trackColor = Color.White.copy(alpha = 0.2f),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${(state.totalProgressPercent * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = QuestuaGold
                                    )
                                }
                            }
                        }
                    }

                    // --- ESPAÇAMENTO ENTRE IMAGEM E MISSÕES ---
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // --- Título da Seção ---
                    item {
                        PaddingValues(horizontal = 24.dp, vertical = 8.dp).let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(24.dp)
                                        .background(QuestuaGold, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Missões da Área",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // --- Lista de Missões ---
                    items(state.quests) { questItem ->
                        QuestItemCard(
                            item = questItem,
                            onClick = {
                                if (questItem.status == QuestStatus.LOCKED) {
                                    onNavigateToUnlock(questItem.quest.id, "QUEST")
                                } else {
                                    onQuestClick(questItem.quest.id)
                                }
                            }
                        )
                    }
                }

                // --- BOTÃO VOLTAR FLUTUANTE ---
                // Mantido fora da LazyColumn para ficar fixo no topo
                SmallFloatingActionButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }

            // Tratamento de Erro
            state.error?.let {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestItemCard(
    item: QuestItemState,
    onClick: () -> Unit
) {
    val isLocked = item.status == QuestStatus.LOCKED
    val isCompleted = item.status == QuestStatus.COMPLETED
    val isAvailable = item.status == QuestStatus.AVAILABLE || item.status == QuestStatus.IN_PROGRESS

    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isAvailable -> QuestuaGold
        else -> Color.Transparent
    }

    val alpha = if (isLocked) 0.6f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .alpha(alpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp),
        border = if (isAvailable || isCompleted) BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFFE8F5E9)
                            isLocked -> MaterialTheme.colorScheme.surfaceVariant
                            else -> QuestuaGold.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isCompleted -> Icons.Default.Check
                        isLocked -> Icons.Default.Lock
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    tint = when {
                        isCompleted -> Color(0xFF2E7D32)
                        isLocked -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> QuestuaGold
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.quest.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isLocked) "Bloqueado" else "${item.quest.xpValue} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isCompleted || item.status == QuestStatus.IN_PROGRESS) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = QuestuaGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = QuestuaGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.userScore}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (isAvailable) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}