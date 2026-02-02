// fileName: app/src/main/java/com/questua/app/presentation/admin/content/questpoints/AdminQuestPointDetailScreen.kt
package com.questua.app.presentation.admin.content.questpoints

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
import com.questua.app.presentation.admin.content.cities.DetailCard // Reutilizando o componente da Cidade se estiver publico, ou copie ele para cá

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestPointDetailScreen(
    navController: NavController,
    viewModel: AdminQuestPointDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    if (showEditDialog && state.questPoint != null) {
        QuestPointFormDialog(
            questPoint = state.questPoint,
            cities = state.cities,
            onDismiss = { showEditDialog = false },
            onConfirm = { title, cId, desc, diff, lat, lon, img, ico, unl, prem, ai, pub ->
                viewModel.saveQuestPoint(title, cId, desc, diff, lat, lon, img, ico, unl, prem, ai, pub)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.questPoint?.title ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.questPoint != null) {
                Surface(tonalElevation = 8.dp, shadowElevation = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Icon(Icons.Default.Delete, null); Text(" EXCLUIR") }

                        Button(onClick = { showEditDialog = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null); Text(" EDITAR")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                state.questPoint?.let { point ->
                    val cityName = state.cities.find { it.id == point.cityId }?.name ?: point.cityId

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                    ) {
                        DetailCard("Principal", listOf(
                            "ID" to point.id,
                            "Título" to point.title,
                            "Cidade" to cityName,
                            "Dificuldade" to "${point.difficulty}/5"
                        ))

                        DetailCard("Mídia e Local", listOf(
                            "Latitude" to point.lat.toString(),
                            "Longitude" to point.lon.toString(),
                            "Imagem" to (point.imageUrl ?: "N/A"),
                            "Ícone" to (point.iconUrl ?: "N/A")
                        ))

                        DetailCard("Requisitos", listOf(
                            "Premium Acesso" to if(point.unlockRequirement?.premiumAccess == true) "Sim" else "Não",
                            "Nível Mín." to (point.unlockRequirement?.requiredGamificationLevel?.toString() ?: "-"),
                            "CEFR Mín." to (point.unlockRequirement?.requiredCefrLevel ?: "-")
                        ))

                        DetailCard("Status", listOf(
                            "Publicado" to if (point.isPublished) "Sim" else "Não",
                            "Premium" to if (point.isPremium) "Sim" else "Não",
                            "Gerado por IA" to if (point.isAiGenerated) "Sim" else "Não"
                        ))

                        DetailCard("Descrição", listOf("" to point.description))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Isso é irreversível.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteQuestPoint()
                        showDeleteDialog = false
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}