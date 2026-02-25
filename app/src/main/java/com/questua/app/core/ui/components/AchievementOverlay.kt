package com.questua.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl // Importante: A extensão que resolve a URL
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.model.Achievement

@Composable
fun AchievementOverlay(
    monitor: AchievementMonitor
) {
    val achievement by monitor.currentPopup.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f) // Garante que fica no topo da Z-Order
            .padding(top = 48.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = achievement != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            achievement?.let { item ->
                AchievementCard(item)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    // Cores do Card
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )
    val goldColor = Color(0xFFFFC107) // QuestuaGold

    Row(
        modifier = Modifier
            .widthIn(min = 300.dp, max = 360.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(2.dp, goldColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Área do Ícone
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(goldColor.copy(alpha = 0.1f)) // Fundo dourado suave
                .border(1.dp, goldColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val iconUrl = achievement.iconUrl

            if (!iconUrl.isNullOrBlank()) {
                // --- LÓGICA CORRIGIDA (IGUAL AO HUB) ---
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(iconUrl.toFullImageUrl()) // Usa a extensão para completar a URL
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    // Fallback para ícone padrão se falhar o carregamento
                    error = rememberVectorPainter(Icons.Default.EmojiEvents)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = goldColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CONQUISTA DESBLOQUEADA!",
                style = MaterialTheme.typography.labelSmall,
                color = goldColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (achievement.xpReward > 0) {
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}