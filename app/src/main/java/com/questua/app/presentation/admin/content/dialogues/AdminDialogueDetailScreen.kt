package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDialogueDetailScreen(
    navController: NavController,
    viewModel: AdminDialogueDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    if (showEditDialog && state.dialogue != null) {
        DialogueFormDialog(
            dialogue = state.dialogue,
            onDismiss = { showEditDialog = false },
            onConfirm = { text, desc, bg, speaker, expects, mode, next ->
                viewModel.saveDialogue(text, desc, bg, speaker, expects, mode, next)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Diálogo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.dialogue != null && !state.isLoading) {
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Excluir")
                        }
                        Button(onClick = { showEditDialog = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Editar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
            } else {
                state.dialogue?.let { dialogue ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DialogueInfoCard("Conteúdo", listOf(
                            "ID" to dialogue.id,
                            "Texto" to dialogue.textContent,
                            "Descrição" to dialogue.description
                        ))

                        DialogueInfoCard("Configuração Técnica", listOf(
                            "Speaker ID" to (dialogue.speakerCharacterId ?: "Nenhum"),
                            "Próximo ID" to (dialogue.nextDialogueId ?: "Fim de Cena"),
                            "Modo de Input" to dialogue.inputMode.name,
                            "Espera Resposta" to if (dialogue.expectsUserResponse) "Sim" else "Não"
                        ))

                        DialogueInfoCard("Visual", listOf(
                            "URL Background" to dialogue.backgroundUrl
                        ))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir este diálogo permanentemente?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteDialogue()
                        showDeleteDialog = false
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun DialogueInfoCard(title: String, items: List<Pair<String, String>>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            items.forEach { (label, value) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}