package com.questua.app.presentation.game

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.Choice
import kotlinx.coroutines.delay

// Cores de Feedback (Estilo Duolingo/Gamificado)
private val SuccessGreen = Color(0xFF58CC02)
private val ErrorRed = Color(0xFFFF4B4B)
private val OverlayScrim = Color(0xFF000000).copy(alpha = 0.4f)

@Composable
fun DialogueScreen(
    onNavigateBack: () -> Unit,
    onQuestCompleted: (String, Int, Int, Int) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Estado para controlar o replay do áudio
    var audioReplayTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.navigateToResult) {
        if (state.navigateToResult) {
            state.userQuestId?.let { id ->
                onQuestCompleted(id, state.xpEarned, state.correctAnswers, state.totalQuestions)
                viewModel.onResultNavigationHandled()
            }
        }
    }

    // --- SISTEMA DE ÁUDIO ---
    AudioHandler(
        bgMusicUrl = state.currentDialogue?.bgMusicUrl,
        voiceUrl = state.currentDialogue?.audioUrl,
        replayTrigger = audioReplayTrigger
    )

    Scaffold(
        containerColor = Color.Black // Fundo base preto para imersão
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- CAMADA 1: Cenário (Background) ---
            state.currentDialogue?.backgroundUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url.toFullImageUrl())
                        .crossfade(1000)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Scrim Gradiente (Escurece a parte de baixo para facilitar leitura)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0.4f
                        )
                    )
            )

            // --- CAMADA 2: Personagem ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 220.dp), // Espaço para a caixa de texto não cobrir o rosto
                contentAlignment = Alignment.BottomCenter // Centralizado ou BottomEnd
            ) {
                state.speaker?.avatarUrl?.let { avatarUrl ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl.toFullImageUrl())
                                .crossfade(true)
                                .build(),
                            contentDescription = state.speaker?.name,
                            // CRUCIAL: Fit garante a proporção correta (não alarga).
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxHeight(0.85f) // Ocupa 85% da altura da tela (Visual Novel style)
                                .widthIn(max = 800.dp) // Limita largura máxima para tablets
                        )
                    }
                }
            }

            // --- CAMADA 3: HUD Superior (Barra de Progresso) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                DialogueHUD(
                    progress = state.questProgress,
                    xp = state.xpEarned,
                    onClose = onNavigateBack
                )
            }

            // --- CAMADA 4: Caixa de Diálogo e Interação ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding() // Sobe com o teclado
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LoadingSpinner()
                    }
                } else {
                    state.currentDialogue?.let { dialogue ->
                        VNTextBox(
                            speakerName = state.speaker?.name ?: "???",
                            text = dialogue.textContent,
                            inputMode = dialogue.inputMode,
                            userInput = state.userInput,
                            choices = dialogue.choices,
                            isSubmitting = state.isSubmitting,
                            hasAudio = !dialogue.audioUrl.isNullOrBlank(),
                            onInputChange = viewModel::onUserInputChange,
                            onTextSubmit = viewModel::onSubmitText,
                            onChoiceClick = viewModel::onChoiceSelected,
                            onContinueClick = viewModel::onContinue,
                            onReplayAudio = { audioReplayTrigger++ }
                        )
                    }
                }
            }

            // --- CAMADA 5: Feedback Overlay (Z-Index Máximo) ---
            // Fica por cima de TUDO, alinhado ao fundo
            FeedbackOverlay(
                state = state.feedbackState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// --- COMPONENTES VISUAIS (ESTÉTICA QUESTUA) ---

@Composable
fun VNTextBox(
    speakerName: String,
    text: String,
    inputMode: InputMode,
    userInput: String,
    choices: List<Choice>?,
    isSubmitting: Boolean,
    hasAudio: Boolean,
    onInputChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onChoiceClick: (Choice) -> Unit,
    onContinueClick: () -> Unit,
    onReplayAudio: () -> Unit
) {
    // Card estilo "Vidro Fosco" ou Sólido do App
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 0.dp),
        shape = RoundedCornerShape(24.dp), // Estilo Questua (arredondado)
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), // Quase sólido para legibilidade
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Cabeçalho: Nome + Botão de Áudio
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = speakerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (hasAudio) {
                    IconButton(
                        onClick = onReplayAudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Ouvir novamente",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Texto do Diálogo
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Área de Interação
            InteractionArea(
                inputMode, userInput, choices, isSubmitting,
                onInputChange, onTextSubmit, onChoiceClick, onContinueClick
            )
        }
    }
}

@Composable
fun InteractionArea(
    inputMode: InputMode,
    userInput: String,
    choices: List<Choice>?,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
    onTextSubmit: () -> Unit,
    onChoiceClick: (Choice) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (inputMode) {
            InputMode.CHOICE -> {
                choices?.forEach { choice ->
                    // Botão estilo Questua/Duolingo
                    OutlinedButton(
                        onClick = { onChoiceClick(choice) },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = choice.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            InputMode.TEXT -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuestuaTextField(
                        value = userInput,
                        onValueChange = onInputChange,
                        label = "Sua resposta...",
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Botão Enviar
                    FilledIconButton(
                        onClick = onTextSubmit,
                        modifier = Modifier.size(56.dp),
                        enabled = !isSubmitting && userInput.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        }
                    }
                }
            }
            InputMode.NONE -> {
                QuestuaButton(
                    text = "CONTINUAR",
                    onClick = onContinueClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DialogueHUD(progress: Float, xp: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fundo translúcido no botão X para contraste
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Sair", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Barra de Progresso
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.3f),
        )
    }
}

@Composable
fun FeedbackOverlay(state: FeedbackState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state !is FeedbackState.None,
        enter = slideInVertically { it } + fadeIn(), // Sobe do fundo
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier // Recebe o alinhamento do pai
    ) {
        val (bgColor, textColor, msg) = when (state) {
            is FeedbackState.Success -> Triple(SuccessGreen, Color.White, state.message ?: "Correto!")
            is FeedbackState.Error -> Triple(ErrorRed, Color.White, state.message ?: "Incorreto!")
            else -> Triple(Color.Transparent, Color.Transparent, "")
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Margem flutuante
            color = bgColor,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone de status (opcional) ou apenas texto grande
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

// --- ENGINE DE ÁUDIO OTIMIZADA ---

@Composable
fun AudioHandler(
    bgMusicUrl: String?,
    voiceUrl: String?,
    replayTrigger: Int
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val bgmPlayer = remember { MediaPlayer() }
    val voicePlayer = remember { MediaPlayer() }

    var currentBgmUrl by remember { mutableStateOf<String?>(null) }
    var currentVoiceUrl by remember { mutableStateOf<String?>(null) }

    // 1. Background Music (Volume Baixo)
    LaunchedEffect(bgMusicUrl) {
        if (bgMusicUrl != null && bgMusicUrl != currentBgmUrl) {
            try {
                if (bgmPlayer.isPlaying) bgmPlayer.stop()
                bgmPlayer.reset()

                val fullUrl = bgMusicUrl.toFullImageUrl()
                bgmPlayer.setDataSource(fullUrl)
                bgmPlayer.isLooping = true
                // VOLUME: Esquerda e Direita em 15% (Background)
                bgmPlayer.setVolume(0.15f, 0.15f)
                bgmPlayer.prepareAsync()
                bgmPlayer.setOnPreparedListener { it.start() }

                currentBgmUrl = bgMusicUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (bgMusicUrl == null) {
            if (bgmPlayer.isPlaying) bgmPlayer.stop()
            currentBgmUrl = null
        }
    }

    // 2. Voz do Personagem (Volume Alto)
    fun playVoice(url: String?) {
        if (url == null) return
        try {
            if (voicePlayer.isPlaying) voicePlayer.stop()
            voicePlayer.reset()

            val fullUrl = url.toFullImageUrl()
            voicePlayer.setDataSource(fullUrl)
            // VOLUME: 100% (Destaque)
            voicePlayer.setVolume(1.0f, 1.0f)
            voicePlayer.prepareAsync()
            voicePlayer.setOnPreparedListener { it.start() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Toca quando a URL muda (novo diálogo)
    LaunchedEffect(voiceUrl) {
        if (voiceUrl != null) {
            currentVoiceUrl = voiceUrl
            playVoice(voiceUrl)
        }
    }

    // Toca quando o botão de replay é clicado
    LaunchedEffect(replayTrigger) {
        if (replayTrigger > 0 && currentVoiceUrl != null) {
            playVoice(currentVoiceUrl)
        }
    }

    // Lifecycle Management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (bgmPlayer.isPlaying) bgmPlayer.pause()
                if (voicePlayer.isPlaying) voicePlayer.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (currentBgmUrl != null && !bgmPlayer.isPlaying) bgmPlayer.start()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bgmPlayer.release()
            voicePlayer.release()
        }
    }
}