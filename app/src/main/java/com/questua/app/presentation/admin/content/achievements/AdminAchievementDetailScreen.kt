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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAchievementDetailScreen(
    navController: NavController,
    viewModel: AdminAchievementDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) navController.popBackStack()
    }

    if (showEditDialog && state.achievement != null) {
        AchievementFormDialog(
            achievement = state.achievement,
            onDismiss = { showEditDialog = false },
            onConfirm = { name: String, desc: String, icon: String, xp: Int, key: String, rarity: RarityType ->
                viewModel.saveAchievement(name, desc, icon, xp, key, rarity)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Conquista") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.achievement != null && !state.isLoading) {
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
                Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else {
                state.achievement?.let { ach ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AchievementInfoCard("Informações Gerais", listOf(
                            "ID" to ach.id,
                            "Nome" to ach.name,
                            "Descrição" to (ach.description ?: "Sem descrição"),
                            "Chave do Sistema" to (ach.keyName ?: "N/A")
                        ))

                        AchievementInfoCard("Recompensas e Raridade", listOf(
                            "Recompensa de XP" to ach.xpReward.toString(),
                            "Raridade" to ach.rarity.name
                        ))

                        AchievementInfoCard("Recursos Visuais", listOf(
                            "URL do Ícone" to (ach.iconUrl ?: "N/A")
                        ))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Excluir Conquista") },
                text = { Text("Deseja excluir permanentemente esta conquista?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAchievement()
                        showDeleteDialog = false
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun AchievementInfoCard(title: String, items: List<Pair<String, String>>) {
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

@Composable
fun AchievementFormDialog(
    achievement: Achievement?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, RarityType) -> Unit
) {
    var name by remember { mutableStateOf(achievement?.name ?: "") }
    var desc by remember { mutableStateOf(achievement?.description ?: "") }
    var icon by remember { mutableStateOf(achievement?.iconUrl ?: "") }
    var xp by remember { mutableStateOf(achievement?.xpReward?.toString() ?: "0") }
    var key by remember { mutableStateOf(achievement?.keyName ?: "") }
    var rarity by remember { mutableStateOf(achievement?.rarity ?: RarityType.COMMON) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (achievement == null) "Nova Conquista" else "Editar") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição")
                QuestuaTextField(value = icon, onValueChange = { icon = it }, label = "URL Ícone")
                QuestuaTextField(value = xp, onValueChange = { xp = it }, label = "XP")
                QuestuaTextField(value = key, onValueChange = { key = it }, label = "Key Name")

                Text("Raridade", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RarityType.entries.forEach { type ->
                        FilterChip(
                            selected = rarity == type,
                            onClick = { rarity = type },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, desc, icon, xp.toIntOrNull() ?: 0, key, rarity) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}