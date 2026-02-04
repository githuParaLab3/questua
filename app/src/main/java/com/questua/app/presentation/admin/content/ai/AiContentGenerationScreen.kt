package com.questua.app.presentation.admin.content.ai

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.CharacterEntity

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
                    Toast.makeText(context, "Criado com sucesso!", Toast.LENGTH_SHORT).show()
                    onNavigateToDetail(event.route)
                }
                is AiContentGenerationViewModel.NavigationEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerar com IA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("O que deseja criar?", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.selectedType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Conteúdo") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AiContentType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.onTypeSelected(type)
                                expanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                text = "Gerar e Visualizar",
                onClick = { viewModel.generate() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
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

    when (type) {
        AiContentType.QUEST_POINT -> {
            val selectedCity = cities.find { it.id == fields["cityId"] }
            OutlinedCard(
                onClick = { showCitySelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(selectedCity?.name ?: "Selecionar Cidade") },
                    supportingContent = { Text(if (fields["cityId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["cityId"]?.takeLast(8)}") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                label = "Tema (Ex: Biblioteca Abandonada)"
            )
        }
        AiContentType.QUEST -> {
            val selectedQP = questPoints.find { it.id == fields["questPointId"] }
            OutlinedCard(
                onClick = { showQuestPointSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(selectedQP?.title ?: "Selecionar Quest Point") },
                    supportingContent = { Text(if (fields["questPointId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["questPointId"]?.takeLast(8)}") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
            QuestuaTextField(
                value = fields["difficulty"] ?: "1",
                onValueChange = { onUpdate("difficulty", it) },
                label = "Nível de Dificuldade (1-10)"
            )
        }
        AiContentType.CHARACTER -> {
            QuestuaTextField(
                value = fields["archetype"] ?: "",
                onValueChange = { onUpdate("archetype", it) },
                label = "Arquétipo do Personagem"
            )
        }
        AiContentType.SCENE_DIALOGUE -> {
            val selectedSpeaker = characters.find { it.id == fields["speakerId"] }
            OutlinedCard(
                onClick = { showCharacterSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(selectedSpeaker?.name ?: "Selecionar Personagem") },
                    supportingContent = { Text(if (fields["speakerId"].isNullOrEmpty()) "Obrigatório" else "ID: ...${fields["speakerId"]?.takeLast(8)}") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(selectedQuest?.title ?: "Selecionar Quest (Opcional)") },
                    supportingContent = { Text(if (fields["questId"].isNullOrEmpty()) "Opcional" else "ID: ...${fields["questId"]?.takeLast(8)}") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                label = "Ação de Gatilho"
            )
            QuestuaTextField(
                value = fields["difficulty"] ?: "EASY",
                onValueChange = { onUpdate("difficulty", it) },
                label = "Dificuldade (EASY, MEDIUM, HARD)"
            )
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
        title = { Text(title) },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    if (canClear) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable { onClear() },
                                headlineContent = { Text("Nenhum (Remover Seleção)", color = MaterialTheme.colorScheme.error) }
                            )
                            HorizontalDivider()
                        }
                    }
                    items(items) { item ->
                        ListItem(
                            modifier = Modifier.clickable { onSelect(item) },
                            headlineContent = { itemContent(item) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}