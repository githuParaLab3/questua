package com.questua.app.presentation.admin.content.ai

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint

val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiContentGenerationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: AiContentGenerationViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AiContentGenerationViewModel.NavigationEvent.Success -> {
                    Toast.makeText(context, "Conteúdo gerado com sucesso!", Toast.LENGTH_SHORT).show()
                    onNavigateToDetail(event.route)
                }
                is AiContentGenerationViewModel.NavigationEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Gerar com IA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente de fundo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                QuestuaGold.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header com Ícone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = QuestuaGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, null, tint = QuestuaGold)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Assistente Criativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Selecione o tipo de conteúdo para gerar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Seletor de Tipo
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tipo de Conteúdo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.selectedType.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        AiContentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name, fontWeight = if(type == state.selectedType) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.onTypeSelected(type)
                                    expanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Campos Dinâmicos
                Text("Parâmetros de Geração", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = QuestuaGold)

                DynamicFormFields(
                    type = state.selectedType,
                    fields = state.fields,
                    cities = state.cities,
                    questPoints = state.questPoints,
                    quests = state.quests,
                    characters = state.characters,
                    onUpdate = { field, value -> viewModel.onFieldUpdate(field, value) }
                )

                Spacer(modifier = Modifier.weight(1f))

                QuestuaButton(
                    text = "Gerar Conteúdo",
                    onClick = { viewModel.generate() },
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
            }
        }
    }
}

@Composable
fun DynamicFormFields(
    type: AiContentType,
    fields: Map<String, String>,
    cities: List<City>,
    questPoints: List<QuestPoint>,
    quests: List<Quest>,
    characters: List<CharacterEntity>,
    onUpdate: (String, String) -> Unit
) {
    var showCitySelector by remember { mutableStateOf(false) }
    var showQuestPointSelector by remember { mutableStateOf(false) }
    var showQuestSelector by remember { mutableStateOf(false) }
    var showCharacterSelector by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (type) {
            AiContentType.QUEST_POINT -> {
                val selectedCity = cities.find { it.id == fields["cityId"] }
                OutlinedCard(
                    onClick = { showCitySelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    ListItem(
                        headlineContent = { Text(selectedCity?.name ?: "Selecionar Cidade", fontWeight = if(selectedCity != null) FontWeight.Bold else FontWeight.Normal) },
                        supportingContent = { Text(if (fields["cityId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["cityId"]?.takeLast(8)}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                if (showCitySelector) {
                    SelectorDialog(
                        title = "Selecione a Cidade",
                        items = cities,
                        itemContent = { Text(it.name, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("cityId", it.id); showCitySelector = false },
                        onDismiss = { showCitySelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["theme"] ?: "",
                    onValueChange = { onUpdate("theme", it) },
                    label = "Tema (Ex: Histórico, Moderno)"
                )
            }
            AiContentType.QUEST -> {
                val selectedQP = questPoints.find { it.id == fields["questPointId"] }
                OutlinedCard(
                    onClick = { showQuestPointSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    ListItem(
                        headlineContent = { Text(selectedQP?.title ?: "Selecionar Quest Point", fontWeight = if(selectedQP != null) FontWeight.Bold else FontWeight.Normal) },
                        supportingContent = { Text(if (fields["questPointId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["questPointId"]?.takeLast(8)}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                if (showQuestPointSelector) {
                    SelectorDialog(
                        title = "Selecione o Quest Point",
                        items = questPoints,
                        itemContent = { Text(it.title, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("questPointId", it.id); showQuestPointSelector = false },
                        onDismiss = { showQuestPointSelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["context"] ?: "",
                    onValueChange = { onUpdate("context", it) },
                    label = "Contexto da Missão"
                )

                Column {
                    Text("Dificuldade (1-5)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = (fields["difficulty"]?.toFloatOrNull() ?: 1f),
                        onValueChange = { onUpdate("difficulty", it.toInt().toString()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = QuestuaGold, activeTrackColor = QuestuaGold)
                    )
                }
            }
            AiContentType.CHARACTER -> {
                QuestuaTextField(
                    value = fields["archetype"] ?: "",
                    onValueChange = { onUpdate("archetype", it) },
                    label = "Arquétipo (Ex: Guia Turístico)"
                )
            }
            AiContentType.SCENE_DIALOGUE -> {
                val selectedSpeaker = characters.find { it.id == fields["speakerId"] }
                OutlinedCard(
                    onClick = { showCharacterSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    ListItem(
                        headlineContent = { Text(selectedSpeaker?.name ?: "Selecionar Personagem", fontWeight = if(selectedSpeaker != null) FontWeight.Bold else FontWeight.Normal) },
                        supportingContent = { Text(if (fields["speakerId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["speakerId"]?.takeLast(8)}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                if (showCharacterSelector) {
                    SelectorDialog(
                        title = "Selecione o Personagem",
                        items = characters,
                        itemContent = { Text(it.name, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("speakerId", it.id); showCharacterSelector = false },
                        onDismiss = { showCharacterSelector = false }
                    )
                }

                val selectedQuest = quests.find { it.id == fields["questId"] }
                OutlinedCard(
                    onClick = { showQuestSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    ListItem(
                        headlineContent = { Text(selectedQuest?.title ?: "Selecionar Quest (Opcional)", fontWeight = if(selectedQuest != null) FontWeight.Bold else FontWeight.Normal) },
                        supportingContent = { Text(if (fields["questId"].isNullOrEmpty()) "Opcional" else "ID: ...${fields["questId"]?.takeLast(8)}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                if (showQuestSelector) {
                    SelectorDialog(
                        title = "Selecione a Quest",
                        items = quests,
                        itemContent = { Text(it.title, fontWeight = FontWeight.Bold) },
                        onSelect = { onUpdate("questId", it.id); showQuestSelector = false },
                        onDismiss = { showQuestSelector = false },
                        canClear = true,
                        onClear = { onUpdate("questId", ""); showQuestSelector = false }
                    )
                }

                QuestuaTextField(
                    value = fields["context"] ?: "",
                    onValueChange = { onUpdate("context", it) },
                    label = "Contexto do Diálogo"
                )
            }
            AiContentType.ACHIEVEMENT -> {
                QuestuaTextField(
                    value = fields["trigger"] ?: "",
                    onValueChange = { onUpdate("trigger", it) },
                    label = "Ação de Gatilho (Ex: Completar 5 quests)"
                )

                // Simple dropdown for Difficulty if needed, or text field for simplicity
                QuestuaTextField(
                    value = fields["difficulty"] ?: "EASY",
                    onValueChange = { onUpdate("difficulty", it) },
                    label = "Dificuldade (EASY, MEDIUM, HARD)"
                )
            }
        }
    }
}

@Composable
fun <T> SelectorDialog(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    canClear: Boolean = false,
    onClear: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    if (canClear) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable { onClear() },
                                headlineContent = { Text("Nenhum (Remover Seleção)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                    items(items) { item ->
                        ListItem(
                            modifier = Modifier.clickable { onSelect(item) },
                            headlineContent = { itemContent(item) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text("Fechar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}