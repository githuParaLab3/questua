package com.questua.app.presentation.admin.content.characters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Persona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCharacterScreen(
    navController: NavController,
    viewModel: AdminCharacterViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current

    var showFormDialog by remember { mutableStateOf<CharacterEntity?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var characterToDelete by remember { mutableStateOf<CharacterEntity?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchCharacters()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        CharacterFormDialog(
            character = showFormDialog,
            onDismiss = {
                isCreating = false
                showFormDialog = null
            },
            onConfirm = { name, avatarUrl, isAi, voiceUrl, persona ->
                viewModel.saveCharacter(
                    id = showFormDialog?.id,
                    name = name,
                    avatarUrl = avatarUrl,
                    isAi = isAi,
                    voiceUrl = voiceUrl,
                    persona = persona
                )
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (characterToDelete != null) {
        AlertDialog(
            onDismissRequest = { characterToDelete = null },
            title = { Text("Excluir Personagem") },
            text = { Text("Deseja excluir '${characterToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCharacter(characterToDelete!!.id)
                    characterToDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { characterToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personagens", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Novo Personagem")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar personagem...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.characters) { char ->
                        ListItem(
                            headlineContent = { Text(char.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Text(if (char.isAiGenerated) "Gerado por IA" else "Manual")
                            },
                            leadingContent = {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                    AsyncImage(
                                        model = char.avatarUrl.toFullImageUrl(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = char }) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { characterToDelete = char }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterFormDialog(
    character: CharacterEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String?, Persona) -> Unit
) {
    var name by remember { mutableStateOf(character?.name ?: "") }
    var avatarUrl by remember { mutableStateOf(character?.avatarUrl ?: "") }
    var isAi by remember { mutableStateOf(character?.isAiGenerated ?: false) }
    var voiceUrl by remember { mutableStateOf(character?.voiceUrl ?: "") }

    // Campos da Persona
    var description by remember { mutableStateOf(character?.persona?.description ?: "") }
    var background by remember { mutableStateOf(character?.persona?.background ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (character == null) "Novo Personagem" else "Editar Personagem") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = avatarUrl, onValueChange = { avatarUrl = it }, label = "URL do Avatar")
                QuestuaTextField(value = voiceUrl, onValueChange = { voiceUrl = it }, label = "URL da Voz (Opcional)")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAi, onCheckedChange = { isAi = it })
                    Text("Gerado por IA")
                }

                HorizontalDivider()
                Text("Persona", style = MaterialTheme.typography.labelLarge)

                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = background, onValueChange = { background = it }, label = "Histórico (Background)")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val persona = Persona(
                        description = description.takeIf { it.isNotBlank() },
                        background = background.takeIf { it.isNotBlank() }
                    )
                    onConfirm(name, avatarUrl, isAi, voiceUrl.takeIf { it.isNotBlank() }, persona)
                },
                enabled = name.isNotBlank() && avatarUrl.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}