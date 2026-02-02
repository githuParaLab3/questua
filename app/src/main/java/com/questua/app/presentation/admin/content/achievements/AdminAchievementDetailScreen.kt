package com.questua.app.presentation.admin.content.achievements

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
fun AdminAchievementDetailScreen(
    navController: NavController,
    viewModel: AdminAchievementDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.achievement != null) {
        AchievementFormDialog(
            achievement = state.achievement,
            onDismiss = { showEdit = false },
            onConfirm = { key, name, desc, icon, rar, xp, meta ->
                viewModel.saveAchievement(key, name, desc, icon, rar, xp, meta)
                showEdit = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.achievement?.name ?: "Detalhes") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            if (state.achievement != null) {
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
        if (state.achievement != null) {
            Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                DetailCard("Principal", listOf(
                    "Nome" to state.achievement.name,
                    "Chave" to state.achievement.keyName,
                    "Raridade" to state.achievement.rarity.name,
                    "XP" to state.achievement.xpReward.toString()
                ))

                DetailCard("Visual", listOf(
                    "Ícone" to (state.achievement.iconUrl ?: "N/A"),
                    "Descrição" to (state.achievement.description ?: "N/A")
                ))

                state.achievement.metadata?.let { meta ->
                    DetailCard("Metadados", listOf(
                        "Categoria" to (meta.category ?: "-"),
                        "Info Extra" to (meta.descriptionExtra ?: "-")
                    ))
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Excluir Conquista") },
                text = { Text("Tem certeza?") },
                confirmButton = { TextButton(onClick = { viewModel.deleteAchievement(); showDelete = false }) { Text("Excluir", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
            )
        }
    }
}