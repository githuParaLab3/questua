package com.questua.app.presentation.admin.content.ai

import android.widget.Toast
import androidx.compose.foundation.layout.*
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

            // Seletor de Tipo de Conteúdo
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Formulário Dinâmico
            DynamicFormFields(state.selectedType, state.fields) { field, value ->
                viewModel.onFieldUpdate(field, value)
            }

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
    onUpdate: (String, String) -> Unit
) {
    when (type) {
        AiContentType.QUEST_POINT -> {
            QuestuaTextField(
                value = fields["cityId"] ?: "",
                onValueChange = { onUpdate("cityId", it) },
                label = "ID da Cidade (UUID)"
            )
            QuestuaTextField(
                value = fields["theme"] ?: "",
                onValueChange = { onUpdate("theme", it) },
                label = "Tema (Ex: Biblioteca Abandonada)"
            )
        }
        AiContentType.QUEST -> {
            QuestuaTextField(
                value = fields["questPointId"] ?: "",
                onValueChange = { onUpdate("questPointId", it) },
                label = "ID do Quest Point (UUID)"
            )
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
            QuestuaTextField(
                value = fields["speakerId"] ?: "",
                onValueChange = { onUpdate("speakerId", it) },
                label = "ID do Personagem (UUID)"
            )
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