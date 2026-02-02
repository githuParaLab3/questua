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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.presentation.admin.content.cities.DetailCard // Certifique-se de que este import está correto para o seu projeto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestDetailScreen(
    navController: NavController,
    viewModel: AdminQuestDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    if (showEdit && state.quest != null) {
        QuestFormDialog(
            quest = state.quest,
            questPoints = state.questPoints,
            dialogues = state.dialogues,
            onDismiss = { showEdit = false },
            onConfirm = { title, qpId, dial, desc, diff, ord, xp, unl, foc, prem, ai, pub ->
                viewModel.saveQuest(qpId, dial, title, desc, diff, ord, xp, unl, foc, prem, ai, pub)
                showEdit = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.quest?.title ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.quest != null) {
                Surface(tonalElevation = 8.dp, shadowElevation = 10.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null); Text(" EXCLUIR")
                        }
                        Button(onClick = { showEdit = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null); Text(" EDITAR")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.quest != null) {
            // Tenta encontrar o nome do QuestPoint na lista carregada, ou usa o ID como fallback
            val qpName = state.questPoints.find { it.id == state.quest.questPointId }?.title ?: state.quest.questPointId

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DetailCard("Principal", listOf(
                    "Título" to state.quest.title,
                    "Quest Point" to qpName,
                    "Diálogo Inicial" to (state.quest.firstDialogueId ?: "Nenhum"),
                    "Ordem" to state.quest.orderIndex.toString()
                ))

                // --- BLOCO QUE FALTAVA ---
                state.quest.unlockRequirement?.let { unlock ->
                    DetailCard("Requisitos de Desbloqueio", listOf(
                        "Exige Premium" to if(unlock.premiumAccess) "Sim" else "Não",
                        "Nível Mín." to (unlock.requiredGamificationLevel?.toString() ?: "-"),
                        "CEFR Mín." to (unlock.requiredCefrLevel ?: "-")
                    ))
                }
                // -------------------------

                state.quest.learningFocus?.let { focus ->
                    DetailCard("Foco de Aprendizado", listOf(
                        "Gramática" to (focus.grammarTopics?.joinToString(", ") ?: "-"),
                        "Vocabulário" to (focus.vocabularyThemes?.joinToString(", ") ?: "-"),
                        "Skills" to (focus.skills?.joinToString(", ") ?: "-")
                    ))
                }

                DetailCard("Configurações & Status", listOf(
                    "Dificuldade" to "${state.quest.difficulty}/5",
                    "XP Recompensa" to "${state.quest.xpValue}",
                    "Conteúdo Premium" to if(state.quest.isPremium) "Sim" else "Não",
                    "Publicado" to if(state.quest.isPublished) "Sim" else "Não",
                    "Gerado por IA" to if(state.quest.isAiGenerated) "Sim" else "Não",
                    "Criado em" to state.quest.createdAt
                ))

                DetailCard("Descrição", listOf("" to state.quest.description))
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Excluir Quest") },
                text = { Text("Tem certeza? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteQuest(); showDelete = false }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
            )
        }
    }
}