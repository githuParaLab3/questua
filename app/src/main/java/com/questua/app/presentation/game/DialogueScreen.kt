package com.questua.app.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onQuestCompleted: (String) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isQuestCompleted) {
        if (state.isQuestCompleted) {
            state.userQuestId?.let { onQuestCompleted(it) }
        }
    }

    Scaffold(
        topBar = {
            DialogueTopBar(
                progress = state.questProgress,
                onClose = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background da Cena
            state.currentDialogue?.backgroundUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.2f)
                )
            }

            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                state.currentDialogue?.let { dialogue ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Avatar e Balão de Fala
                        CharacterSection(
                            dialogue = dialogue,
                            speaker = state.speaker
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Área de Interação (Botões ou Input de Texto)
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

                        // Feedback (Sucesso/Erro)
                        FeedbackOverlay(state.feedbackState)

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Tratamento de Erro Global
            state.error?.let {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Sair")
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Progresso de 0 a 100 convertido para 0.0 a 1.0
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun CharacterSection(dialogue: SceneDialogue, speaker: CharacterEntity?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Avatar
        Box(modifier = Modifier.size(120.dp)) {
            if (speaker?.avatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(speaker.avatarUrl.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                // Placeholder se não tiver personagem (Narrador)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balão de Texto
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Nome
                Text(
                    text = speaker?.name ?: "Narrador",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Texto
                Text(
                    text = dialogue.textContent,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
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
                    QuestuaButton(
                        text = choice.text,
                        onClick = { onChoiceClick(choice) },
                        isSecondary = true,
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
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
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
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

@Composable
fun FeedbackOverlay(state: FeedbackState) {
    AnimatedVisibility(
        visible = state !is FeedbackState.None,
        enter = slideInVertically { it } + fadeIn()
    ) {
        val (bgColor, textColor, text) = when (state) {
            is FeedbackState.Success -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), state.message)
            is FeedbackState.Error -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), state.message)
            else -> Triple(Color.Transparent, Color.Transparent, "")
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = bgColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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