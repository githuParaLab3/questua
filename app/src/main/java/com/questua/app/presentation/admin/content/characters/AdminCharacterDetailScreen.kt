package com.questua.app.presentation.admin.content.characters

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
fun AdminCharacterDetailScreen(
    navController: NavController,
    viewModel: AdminCharacterDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.character != null) {
        CharacterFormDialog(
            character = state.character,
            onDismiss = { showEdit = false },
            onConfirm = { name, av, vc, sp, per, ai ->
                viewModel.saveCharacter(name, av, vc, sp, per, ai)
                showEdit = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.character?.name ?: "Detalhes") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            if (state.character != null) {
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
        if (state.character != null) {
            Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                DetailCard("Info Básica", listOf(
                    "Nome" to state.character.name,
                    "ID" to state.character.id,
                    "Gerado por IA" to if(state.character.isAiGenerated) "Sim" else "Não",
                    "Criado em" to state.character.createdAt
                ))

                DetailCard("Mídia", listOf(
                    "Avatar URL" to state.character.avatarUrl,
                    "Voz URL" to (state.character.voiceUrl ?: "Não possui"),
                    "Sprites" to "${state.character.spriteSheet?.urls?.size ?: 0} imagens"
                ))

                state.character.persona?.let { p ->
                    DetailCard("Persona: Descrição", listOf("" to (p.description ?: "Sem descrição")))

                    DetailCard("Persona: Atributos", listOf(
                        "Estilo de Fala" to (p.speakingStyle ?: "-"),
                        "Tom de Voz" to (p.voiceTone ?: "-"),
                        "Background" to (p.background ?: "-")
                    ))

                    if (p.traits.isNotEmpty()) {
                        DetailCard("Persona: Traços", p.traits.mapIndexed { i, t -> "Traço ${i+1}" to t })
                    }
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Excluir Personagem") },
                text = { Text("Tem certeza? Esta ação é irreversível.") },
                confirmButton = { TextButton(onClick = { viewModel.deleteCharacter(); showDelete = false }) { Text("Excluir", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
            )
        }
    }
}