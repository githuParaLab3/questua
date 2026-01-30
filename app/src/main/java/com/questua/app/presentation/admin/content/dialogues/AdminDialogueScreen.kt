package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.SceneDialogue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDialogueScreen(
    navController: NavController,
    viewModel: AdminDialogueViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<SceneDialogue?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var dialogueToDelete by remember { mutableStateOf<SceneDialogue?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchDialogues()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        DialogueFormDialog(
            dialogue = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { text, desc, bg, speaker, expects, mode, next ->
                viewModel.saveDialogue(showFormDialog?.id, text, desc, bg, speaker, expects, mode, next)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (dialogueToDelete != null) {
        AlertDialog(
            onDismissRequest = { dialogueToDelete = null },
            title = { Text("Excluir Diálogo") },
            text = { Text("Deseja excluir este diálogo?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDialogue(dialogueToDelete!!.id)
                    dialogueToDelete = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { dialogueToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Diálogos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Diálogo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar texto...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.dialogues) { dialogue ->
                        ListItem(
                            headlineContent = { Text(dialogue.textContent.take(50) + "...", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("ID: ${dialogue.id}") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = dialogue }) {
                                        Icon(Icons.Default.Edit, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                                    }
                                    IconButton(onClick = { dialogueToDelete = dialogue }) {
                                        Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null)
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
fun DialogueFormDialog(
    dialogue: SceneDialogue?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, Boolean, InputMode, String?) -> Unit
) {
    var textContent by remember { mutableStateOf(dialogue?.textContent ?: "") }
    var description by remember { mutableStateOf(dialogue?.description ?: "") }
    var backgroundUrl by remember { mutableStateOf(dialogue?.backgroundUrl ?: "") }
    var speakerId by remember { mutableStateOf(dialogue?.speakerCharacterId ?: "") }
    var expectsResponse by remember { mutableStateOf(dialogue?.expectsUserResponse ?: false) }
    var inputMode by remember { mutableStateOf(dialogue?.inputMode ?: InputMode.TEXT) }
    var nextId by remember { mutableStateOf(dialogue?.nextDialogueId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dialogue == null) "Novo Diálogo" else "Editar Diálogo") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                QuestuaTextField(value = textContent, onValueChange = { textContent = it }, label = "Texto")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = backgroundUrl, onValueChange = { backgroundUrl = it }, label = "URL BG")
                QuestuaTextField(value = speakerId, onValueChange = { speakerId = it }, label = "ID Falante")
                QuestuaTextField(value = nextId, onValueChange = { nextId = it }, label = "ID Próximo")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = expectsResponse, onCheckedChange = { expectsResponse = it })
                    Text("Espera resposta?")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(textContent, description, backgroundUrl, speakerId.takeIf { it.isNotBlank() }, expectsResponse, inputMode, nextId.takeIf { it.isNotBlank() })
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}