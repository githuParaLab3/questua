package com.questua.app.presentation.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.core.ui.theme.*

@Composable
fun HubDashboard(
    userName: String,
    userStreak: Int,
    userXp: Int,
    languageCode: String?,
    languageIconUrl: String?,
    onQuestClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderStats(
            streak = userStreak,
            xp = userXp,
            languageCode = languageCode,
            languageIconUrl = languageIconUrl,
            onLanguageClick = onLanguageClick
        )

        Column(modifier = Modifier.padding(24.dp)) {
            // Saudação
            Text(
                text = "Olá, $userName!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Text(
                text = "Sua jornada continua.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Principal
            CurrentQuestCard(onClick = onQuestClick)

            Spacer(modifier = Modifier.height(32.dp))

            // Seção Desafios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Novos Desafios",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Amber600
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(3) { index ->
                    ChallengeCard(index = index)
                }
            }
        }
    }
}

@Composable
fun CurrentQuestCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = Amber500.copy(0.2f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(100.dp)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(Color(0xFFFFFBEB), RoundedCornerShape(bottomStart = 100.dp))
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Slate50, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Explore, null, tint = Slate500)
                    }

                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "EM PROGRESSO",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Amber600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Conversa no Metrô",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Capítulo 2 • Londres",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Slate50)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(listOf(Amber500, Amber600))
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(index: Int) {
    val color = if (index % 2 == 0) Color(0xFFF3E8FF) else Color(0xFFFFE4E6)
    val iconColor = if (index % 2 == 0) Color(0xFF9333EA) else Color(0xFFE11D48)
    val icon = if (index % 2 == 0) Icons.Default.Book else Icons.Default.Star

    Surface(
        modifier = Modifier.width(160.dp).height(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = "Vocabulário", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate800))
                Text(text = "+20 XP", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
            }
        }
    }
}