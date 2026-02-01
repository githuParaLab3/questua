package com.questua.app.presentation.game

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.theme.Amber500
import com.questua.app.core.ui.theme.Green500
import com.questua.app.domain.enums.ProgressStatus
import kotlinx.coroutines.flow.collectLatest

// As classes QuestIntroUiEvent estão no arquivo do ViewModel (mesmo pacote),
// então o import é automático ou desnecessário se o pacote for igual.

@Composable
fun QuestIntroScreen(
    onNavigateBack: () -> Unit,
    onStartGameplay: (String) -> Unit,
    viewModel: QuestIntroViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // O compilador agora vai encontrar QuestIntroUiEvent corretamente
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is QuestIntroUiEvent.NavigateToGame -> {
                    onStartGameplay(event.questId)
                }
                is QuestIntroUiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (!state.isLoading && state.quest != null) {
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        QuestuaButton(
                            text = when (state.userQuest?.status) {
                                ProgressStatus.COMPLETED -> "Jogar Novamente"
                                ProgressStatus.IN_PROGRESS -> "Continuar Missão"
                                else -> "Iniciar Missão"
                            },
                            // CORREÇÃO: Chamada sem parâmetros (sem chaves {})
                            onClick = {
                                viewModel.onStartQuestClicked()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else if (state.quest != null) {
                val quest = state.quest!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(state.questPoint?.imageUrl?.toFullImageUrl())
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black),
                                        startY = 0f
                                    )
                                )
                        )

                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(top = 48.dp, start = 16.dp)
                                .align(Alignment.TopStart),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.4f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            state.userQuest?.let { uq ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = if (uq.status == ProgressStatus.COMPLETED) "Completada" else "Em Progresso",
                                            color = Color.White
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            if (uq.status == ProgressStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.PlayCircleOutline,
                                            null,
                                            tint = if (uq.status == ProgressStatus.COMPLETED) Green500 else Amber500
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Color.Black.copy(alpha = 0.6f)
                                    ),
                                    border = null
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = quest.title,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (state.questPoint != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = state.questPoint!!.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = quest.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestInfoCard(
                                icon = Icons.Default.Star,
                                label = "XP",
                                value = "${quest.xpValue}",
                                color = Amber500,
                                modifier = Modifier.weight(1f)
                            )
                            QuestInfoCard(
                                icon = Icons.Default.Speed,
                                label = "Dificuldade",
                                value = when(quest.difficulty) {
                                    1 -> "Fácil"
                                    2 -> "Média"
                                    3 -> "Difícil"
                                    else -> "Normal"
                                },
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        state.userQuest?.let { uq ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Seu Progresso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Pontuação Atual", style = MaterialTheme.typography.bodyMedium)
                                        Text("${uq.score} pts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { uq.percentComplete },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        quest.learningFocus?.let { focus ->
                            if (!focus.grammarTopics.isNullOrEmpty() || !focus.vocabularyThemes.isNullOrEmpty()) {
                                Text("O que você vai aprender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                focus.grammarTopics?.forEach { topic ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(topic, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                focus.vocabularyThemes?.forEach { theme ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(theme, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun QuestInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}