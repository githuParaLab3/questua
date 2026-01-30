package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDialogueScreen(
    navController: NavController,
    viewModel: AdminDialogueViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchDialogues()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        DialogueFormDialog(
            dialogue = null,
            onDismiss = { isCreating = false },
            onConfirm = { text, desc, bg, speaker, expects, mode, next ->
                viewModel.saveDialogue(null, text, desc, bg, speaker, expects, mode, next)
                isCreating = false
            }
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
                Icon(Icons.Default.Add, "Novo Diálogo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar diálogo...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.dialogues) { dialogue ->
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.AdminDialogueDetail.passId(dialogue.id))
                            },
                            headlineContent = {
                                Text(dialogue.textContent, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text("Mode: ${dialogue.inputMode} • Speaker: ${dialogue.speakerCharacterId ?: "None"}")
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, tint = MaterialTheme.colorScheme.outline, contentDescription = null)
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
    var text by remember { mutableStateOf(dialogue?.textContent ?: "") }
    var desc by remember { mutableStateOf(dialogue?.description ?: "") }
    var bg by remember { mutableStateOf(dialogue?.backgroundUrl ?: "") }
    var speakerId by remember { mutableStateOf(dialogue?.speakerCharacterId ?: "") }
    var expects by remember { mutableStateOf(dialogue?.expectsUserResponse ?: false) }
    var mode by remember { mutableStateOf(dialogue?.inputMode ?: InputMode.TEXT) }
    var nextId by remember { mutableStateOf(dialogue?.nextDialogueId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dialogue == null) "Novo Diálogo" else "Editar Diálogo") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = text, onValueChange = { text = it }, label = "Texto do Diálogo")
                QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição Interna")
                QuestuaTextField(value = bg, onValueChange = { bg = it }, label = "URL Background")
                QuestuaTextField(value = speakerId, onValueChange = { speakerId = it }, label = "Speaker ID (Opcional)")
                QuestuaTextField(value = nextId, onValueChange = { nextId = it }, label = "Next Dialogue ID (Opcional)")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = expects, onCheckedChange = { expects = it })
                    Text("Espera resposta do usuário")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(text, desc, bg, speakerId.takeIf { it.isNotBlank() }, expects, mode, nextId.takeIf { it.isNotBlank() })
                },
                enabled = text.isNotBlank() && bg.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}