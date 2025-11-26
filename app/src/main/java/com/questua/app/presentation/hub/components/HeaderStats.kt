package com.questua.app.presentation.hub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.core.ui.theme.*

@Composable
fun HeaderStats(
    streak: Int,
    xp: Int,
    languageCode: String?,     // Ex: "PT", "EN" (pode ser nulo carregando)
    languageIconUrl: String?,  // URL da bandeira
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Badge Idioma (Clicável)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onLanguageClick)
                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                .background(Slate50, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone / Bandeira
            if (languageIconUrl != null) {
                QuestuaAsyncImage(
                    imageUrl = languageIconUrl,
                    contentDescription = "Idioma atual",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                // Fallback visual enquanto carrega ou se não houver ícone
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate400) // Cinza neutro
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = languageCode ?: "--", // Mostra traço se ainda não carregou
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
            )
        }

        // Status (Streak e XP)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Streak
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Amber500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = streak.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Amber600
                    )
                )
            }

            // XP
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "XP",
                    tint = QuestuaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$xp XP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = QuestuaBlueDark
                    )
                )
            }
        }
    }
}