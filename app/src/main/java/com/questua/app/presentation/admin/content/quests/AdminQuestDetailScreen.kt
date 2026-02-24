package com.questua.app.presentation.admin.content.quests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.LoadingSpinner

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.quest?.title ?: "Detalhes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            if (state.quest != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EXCLUIR", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showEdit = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuestuaGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EDITAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                QuestuaGold.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            if (state.isLoading) {
                LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else if (state.quest != null) {
                // Tenta encontrar o nome do QuestPoint na lista carregada, ou usa o ID como fallback
                val qpName = state.questPoints.find { it.id == state.quest.questPointId }?.title ?: state.quest.questPointId
                val dialogueName = state.dialogues.find { it.id == state.quest.firstDialogueId }?.textContent?.take(30)?.plus("...") ?: state.quest.firstDialogueId ?: "Nenhum"

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailCard(
                        title = "Principal",
                        items = listOf(
                            "Título" to state.quest.title,
                            "Quest Point" to qpName,
                            "Diálogo Inicial" to dialogueName,
                            "Ordem" to state.quest.orderIndex.toString()
                        )
                    )

                    DetailCard(
                        title = "Configurações & Status",
                        items = listOf(
                            "Dificuldade" to "${state.quest.difficulty}/5",
                            "XP Conclusão" to "${state.quest.xpValue}",
                            "XP por Questão" to "${state.quest.xpPerQuestion}",
                            "Conteúdo Premium" to if (state.quest.isPremium) "Sim" else "Não",
                            "Publicado" to if (state.quest.isPublished) "Sim" else "Não",
                            "Gerado por IA" to if (state.quest.isAiGenerated) "Sim" else "Não",
                            "Criado em" to state.quest.createdAt
                        )
                    )

                    if (state.quest.description.isNotBlank()) {
                        // Usando um card customizado simples para descrição se o DetailCard esperar Pares fixos
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Descrição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = QuestuaGold)
                                Spacer(Modifier.height(8.dp))
                                Text(state.quest.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    state.quest.unlockRequirement?.let { unlock ->
                        DetailCard(
                            title = "Requisitos de Desbloqueio",
                            items = listOf(
                                "Exige Premium" to if (unlock.premiumAccess) "Sim" else "Não",
                                "Nível Mín." to (unlock.requiredGamificationLevel?.toString() ?: "-"),
                                "CEFR Mín." to (unlock.requiredCefrLevel ?: "-")
                            )
                        )
                    }

                    state.quest.learningFocus?.let { focus ->
                        DetailCard(
                            title = "Foco de Aprendizado",
                            items = listOf(
                                "Gramática" to (focus.grammarTopics?.joinToString(", ") ?: "-"),
                                "Vocabulário" to (focus.vocabularyThemes?.joinToString(", ") ?: "-"),
                                "Skills" to (focus.skills?.joinToString(", ") ?: "-")
                            )
                        )
                    }
                }
            }
        }

        if (showEdit && state.quest != null) {
            QuestFormDialog(
                quest = state.quest,
                questPoints = state.questPoints,
                dialogues = state.dialogues,
                onDismiss = { showEdit = false },
                onConfirm = { title, qpId, dial, desc, diff, ord, xpValue, xpPerQuestion, unl, foc, prem, ai, pub ->
                    viewModel.saveQuest(qpId, dial, title, desc, diff, ord, xpValue, xpPerQuestion, unl, foc, prem, ai, pub)
                    showEdit = false
                }
            )
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Quest", fontWeight = FontWeight.Bold) },
                text = { Text("Tem certeza? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteQuest(); showDelete = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDelete = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Cancelar")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun DetailCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = QuestuaGold.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            items.forEach { (label, value) ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}