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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.presentation.admin.content.cities.DetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDialogueDetailScreen(
    navController: NavController,
    viewModel: AdminDialogueDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.dialogue != null) {
        SceneDialogueFormDialog(
            dialogue = state.dialogue,
            characters = state.characters,
            allDialogues = state.allDialogues,
            onDismiss = { showEdit = false },
            onConfirm = { txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai ->
                viewModel.saveDialogue(txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai)
                showEdit = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Diálogo") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            if (state.dialogue != null) {
                Surface(tonalElevation = 8.dp) {
                    Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = { showDelete = true }, Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, null); Text(" EXCLUIR")
                        }
                        Button(onClick = { showEdit = true }, Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null); Text(" EDITAR")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.dialogue != null) {
            val speakerName = state.characters.find { it.id == state.dialogue.speakerCharacterId }?.name ?: "Narrador"
            val nextName = state.allDialogues.find { it.id == state.dialogue.nextDialogueId }?.textContent?.take(20) ?: "Nenhum"

            Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                DetailCard("Geral", listOf(
                    "Descrição" to state.dialogue.description,
                    "Texto" to state.dialogue.textContent,
                    "Falante" to speakerName,
                    "Criado em" to state.dialogue.createdAt
                ))

                DetailCard("Mídia", listOf(
                    "Background" to state.dialogue.backgroundUrl,
                    "Música" to (state.dialogue.bgMusicUrl ?: "-"),
                    "Áudio Fala" to (state.dialogue.audioUrl ?: "-")
                ))

                DetailCard("Fluxo", listOf(
                    "Modo" to state.dialogue.inputMode.name,
                    "Aguarda Resposta" to if(state.dialogue.expectsUserResponse) "Sim" else "Não",
                    "Resposta Esperada" to (state.dialogue.expectedResponse ?: "-"),
                    "Próximo ID" to nextName
                ))

                if (!state.dialogue.choices.isNullOrEmpty()) {
                    DetailCard("Escolhas (${state.dialogue.choices.size})", state.dialogue.choices.mapIndexed { i, c ->
                        "Opção ${i+1}" to "${c.text} -> ${c.nextDialogueId?.take(8) ?: "Fim"}"
                    })
                }

                if (!state.dialogue.sceneEffects.isNullOrEmpty()) {
                    DetailCard("Efeitos (${state.dialogue.sceneEffects.size})", state.dialogue.sceneEffects.map {
                        it.type to "Int: ${it.intensity ?: "-"}, Dur: ${it.duration ?: "-"}"
                    })
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Excluir Diálogo") },
                text = { Text("Tem certeza?") },
                confirmButton = { TextButton(onClick = { viewModel.deleteDialogue(); showDelete = false }) { Text("Excluir", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
            )
        }
    }
}