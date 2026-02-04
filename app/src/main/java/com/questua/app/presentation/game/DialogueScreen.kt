package com.questua.app.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Choice
import com.questua.app.domain.model.SceneDialogue

@Composable
fun DialogueScreen(
    onNavigateBack: () -> Unit,
    onQuestCompleted: (String, Int, Int, Int) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.navigateToResult) {
        if (state.navigateToResult) {
            state.userQuestId?.let { id ->
                onQuestCompleted(id, state.xpEarned, state.correctAnswers, state.totalQuestions)
                viewModel.onResultNavigationHandled()
            }
        }
    }

    Scaffold { paddingValues ->
        // Usamos Box para criar camadas (Layers)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // --- CAMADA 1: Fundo Imersivo ---
            state.currentDialogue?.backgroundUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cenário",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Fallback se não houver imagem
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }

            // Scrim (Gradiente para legibilidade)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)
                            ),
                            startY = 0.3f
                        )
                    )
            )

            // --- Barra de Topo Flutuante ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                DialogueTopBar(
                    progress = state.questProgress,
                    onClose = onNavigateBack
                )
            }


            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                state.currentDialogue?.let { dialogue ->

                    // --- CAMADA 2: Personagem (Speaker) ---
                    // Posicionado na parte inferior direita, "atrás" da caixa de texto mas "na frente" do fundo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 200.dp), // Espaço para a caixa de diálogo
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        state.speaker?.avatarUrl?.let { avatarUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl.toFullImageUrl())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = state.speaker?.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .heightIn(max = 400.dp) // Limita altura
                                    .padding(end = 16.dp)
                            )
                        }
                    }

                    // --- CAMADA 3: Interface de Diálogo (Visual Novel Style) ---
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .imePadding(), // Ajusta teclado
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Feedback flutuante (acima da caixa)
                        FeedbackOverlay(state.feedbackState)

                        // Caixa de Diálogo Principal
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            tonalElevation = 8.dp,
                            shadowElevation = 16.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Nome do Personagem
                                Text(
                                    text = state.speaker?.name ?: "Narrador",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Texto do Diálogo
                                Text(
                                    text = dialogue.textContent,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 20.sp,
                                        lineHeight = 28.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Área de Interação (Opções ou Texto)
                                InteractionSection(
                                    inputMode = dialogue.inputMode,
                                    userInput = state.userInput,
                                    choices = dialogue.choices,
                                    isSubmitting = state.isSubmitting,
                                    onInputChange = viewModel::onUserInputChange,
                                    onTextSubmit = viewModel::onSubmitText,
                                    onChoiceClick = viewModel::onChoiceSelected,
                                    onContinueClick = viewModel::onContinue
                                )
                            }
                        }
                    }
                }
            }

            // Tratamento de Erro Global
            state.error?.let {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
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
fun DialogueTopBar(progress: Float, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão Fechar com fundo translúcido para visibilidade sobre imagem
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Sair", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Barra de progresso mais grossa e visível
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(5.dp)), // Borda sutil
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        )
    }
}

@Composable
fun InteractionSection(
    inputMode: InputMode,
    userInput: String,
    choices: List<Choice>?,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onChoiceClick: (Choice) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (inputMode) {
            InputMode.CHOICE -> {
                choices?.forEach { choice ->
                    QuestuaOptionButton(
                        text = choice.text,
                        onClick = { onChoiceClick(choice) },
                        enabled = !isSubmitting
                    )
                }
            }
            InputMode.TEXT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuestuaTextField(
                        value = userInput,
                        onValueChange = onInputChange,
                        label = "Sua resposta...",
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onTextSubmit,
                        enabled = !isSubmitting && userInput.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                }
            }
            InputMode.NONE -> {
                QuestuaButton(
                    text = "Continuar",
                    onClick = onContinueClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Novo componente de botão estilo "Pílula" / Cartão de Opção
@Composable
fun QuestuaOptionButton(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    enabled: Boolean = true
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FeedbackOverlay(state: FeedbackState) {
    AnimatedVisibility(
        visible = state !is FeedbackState.None,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    ) {
        val (bgColor, textColor, iconColor, text) = when (state) {
            is FeedbackState.Success -> Quadruple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                MaterialTheme.colorScheme.primary,
                state.message
            )
            is FeedbackState.Error -> Quadruple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                MaterialTheme.colorScheme.error,
                state.message
            )
            else -> Quadruple(Color.Transparent, Color.Transparent, Color.Transparent, "")
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = bgColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Espaço entre feedback e caixa de texto
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador visual simples
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text ?: "",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper class simples para o return do when
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)