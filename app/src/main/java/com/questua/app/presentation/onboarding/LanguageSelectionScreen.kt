package com.questua.app.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.core.ui.theme.Amber500
import com.questua.app.core.ui.theme.Slate200
import com.questua.app.core.ui.theme.Slate50
import com.questua.app.core.ui.theme.Slate500
import com.questua.app.core.ui.theme.Slate800
import com.questua.app.core.ui.theme.Slate900
// Helper para rotação que faltava no import
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LanguageSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Slate50)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Imersivo (Navy Blue)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .offset(x = (-12).dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Amber500, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Escolha seu destino",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "O que você vai aprender hoje?",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate200.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }

            // 2. Lista de Idiomas (Sobreposta ao header levemente)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-20).dp) // Efeito de sobreposição
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Slate50)
                    .padding(horizontal = 24.dp)
            ) {
                if (state.isLoading) {
                    LoadingSpinner(transparentBackground = true)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.languages) { language ->
                            LanguageCard(
                                name = language.name,
                                iconUrl = language.iconUrl,
                                onClick = { onLanguageSelected(language.id) }
                            )
                        }
                    }
                }
            }
        }

        state.error?.let { error ->
            ErrorDialog(message = error, onDismiss = { viewModel.clearError() })
        }
    }
}

@Composable
fun LanguageCard(
    name: String,
    iconUrl: String?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp, // Sombra suave
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bandeira / Ícone
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Slate50,
                modifier = Modifier
                    .size(56.dp)
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (iconUrl != null) {
                        QuestuaAsyncImage(
                            imageUrl = iconUrl,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback visual (Iniciais)
                        Text(
                            text = name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Slate500
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                )
                Text(
                    text = "Clique para começar",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500
                    )
                )
            }

            // Seta indicativa
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Usando ArrowBack rotacionada para simular ArrowForward se não tiver o ícone
                contentDescription = null,
                modifier = Modifier.rotate(180f), // Gambiarra visual elegante se não tiver ArrowForward
                tint = Amber500
            )
        }
    }
}

