package com.questua.app.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.questua.app.core.ui.components.QuestuaButton

val QuestuaGold = Color(0xFFFFC107)

@Composable
fun InitialScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Gradiente estilo Questua Gold (Claro/Branco) em vez de escuro,
            // ou mantemos o Dark se for a identidade de abertura, mas adaptado para o Gold.
            // Aqui optei por um fundo escuro elegante que destaca o Dourado,
            // similar ao estilo "Premium" do app.
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF212121), // Cinza/Preto Profundo
                        Color.Black
                    )
                )
            )
    ) {
        // Elemento decorativo: Brilho Dourado
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(QuestuaGold.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.2f),
                    radius = 500.dp.toPx()
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1.6f)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo Container
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .rotate(45f)
                            .background(
                                color = QuestuaGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(32.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .rotate(45f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(QuestuaGold, Color(0xFFFFD54F))
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(-45f),
                            tint = Color.Black // Contraste com o dourado
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Questua",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp,
                        letterSpacing = (-1.5).sp
                    ),
                    color = Color.White
                )

                // Detalhe Gold abaixo do nome
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(3.dp)
                        .width(40.dp)
                        .background(QuestuaGold, RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Aprenda idiomas explorando o mundo",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 19.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Card Inferior
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp),
                shadowElevation = 32.dp
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp)
                        .padding(top = 32.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(50)
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    QuestuaButton(
                        text = "Começar Aventura",
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )

                    QuestuaButton(
                        text = "Já possuo uma conta",
                        onClick = onNavigateToLogin,
                        isSecondary = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )

                    Text(
                        text = "Ao entrar você concorda com nossos Termos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}