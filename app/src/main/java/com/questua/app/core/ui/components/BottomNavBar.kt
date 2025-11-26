package com.questua.app.presentation.hub.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.core.ui.theme.Amber500
import com.questua.app.core.ui.theme.Slate200
import com.questua.app.core.ui.theme.Slate400

enum class HubTab(val icon: ImageVector, val label: String) {
    HOME(Icons.Default.Home, "Home"),
    MAP(Icons.Default.Map, "Mapa"),
    RANK(Icons.Default.EmojiEvents, "Rank"),
    PROFILE(Icons.Default.Person, "Perfil")
}

@Composable
fun BottomNavBar(
    selectedTab: HubTab,
    onTabSelected: (HubTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(elevation = 15.dp, spotColor = Color.Black.copy(alpha = 0.1f)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                // Animações
                val offsetY by animateDpAsState(targetValue = if (isSelected) (-8).dp else 0.dp, label = "offset")
                val iconColor by animateColorAsState(targetValue = if (isSelected) Color.White else Slate400, label = "color")
                val bgColor by animateColorAsState(targetValue = if (isSelected) Amber500 else Color.Transparent, label = "bg")
                val labelColor by animateColorAsState(targetValue = if (isSelected) Amber500 else Slate400, label = "label")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = offsetY)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null // Remove o ripple padrão para ficar igual ao React
                        ) { onTabSelected(tab) }
                ) {
                    // Ícone Container
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = if (isSelected) 10.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Amber500.copy(alpha = 0.5f)
                            )
                            .background(bgColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    )
                }
            }
        }
    }
}