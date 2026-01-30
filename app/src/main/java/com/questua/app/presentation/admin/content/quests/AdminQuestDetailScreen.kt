package com.questua.app.presentation.admin.content.quests

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
fun AdminQuestDetailScreen(
    navController: NavController,
    viewModel: AdminQuestDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    if (showEditDialog && state.quest != null) {
        QuestFormDialog(
            quest = state.quest,
            onDismiss = { showEditDialog = false },
            onConfirm = { pId, t, d, diff, ord, xp, prem, pub ->
                viewModel.saveQuest(pId, t, d, diff, ord, xp, prem, pub)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Quest") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.quest != null && !state.isLoading) {
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
                state.quest?.let { quest ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuestInfoCard("Informações Básicas", listOf(
                            "ID" to quest.id,
                            "Título" to quest.title,
                            "Descrição" to quest.description,
                            "ID Quest Point" to quest.questPointId,
                            "Primeiro Diálogo ID" to (quest.firstDialogueId ?: "Nenhum")
                        ))

                        QuestInfoCard("Configurações de Gameplay", listOf(
                            "Dificuldade" to "${quest.difficulty}/5",
                            "Ordem" to quest.orderIndex.toString(),
                            "Valor de XP" to quest.xpValue.toString()
                        ))

                        QuestInfoCard("Status e Metadados", listOf(
                            "Premium" to if (quest.isPremium) "Sim" else "Não",
                            "Publicada" to if (quest.isPublished) "Sim" else "Não",
                            "Gerada por IA" to if (quest.isAiGenerated) "Sim" else "Não",
                            "Criada em" to quest.createdAt
                        ))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir esta quest permanentemente?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteQuest()
                        showDeleteDialog = false
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun QuestInfoCard(title: String, items: List<Pair<String, String>>) {
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