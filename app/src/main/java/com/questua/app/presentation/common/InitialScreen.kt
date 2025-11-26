package com.questua.app.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border // <--- ADICIONE ESTE IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.theme.Amber400
import com.questua.app.core.ui.theme.Amber600
import com.questua.app.core.ui.theme.Slate200
import com.questua.app.core.ui.theme.Slate900

@Composable
fun InitialScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Conteúdo Superior (Logo e Texto)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo Container
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .rotate(45f)
                        .background(
                            brush = Brush.linearGradient(listOf(Amber400, Amber600)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(4.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(-45f),
                        tint = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Questua",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = Color(0xFFFFFBEB)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aprenda idiomas explorando o mundo",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Slate200.copy(alpha = 0.9f)
                )
            }

            // Card Inferior (Botões)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .background(Slate200, RoundedCornerShape(50))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuestuaButton(
                        text = "Começar Aventura",
                        onClick = onNavigateToRegister
                    )

                    QuestuaButton(
                        text = "Já tenho conta",
                        onClick = onNavigateToLogin,
                        isSecondary = true
                    )
                }
            }
        }
    }
}