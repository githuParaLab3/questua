package com.questua.app.presentation.admin.content.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAchievementScreen(
    navController: NavController,
    viewModel: AdminAchievementViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<Achievement?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var achievementToDelete by remember { mutableStateOf<Achievement?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchAchievements()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        AchievementFormDialog(
            achievement = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { name, desc, icon, xp, key, rarity ->
                viewModel.saveAchievement(showFormDialog?.id, name, desc, icon, xp, key, rarity)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (achievementToDelete != null) {
        AlertDialog(
            onDismissRequest = { achievementToDelete = null },
            title = { Text("Excluir Conquista") },
            text = { Text("Deseja excluir '${achievementToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAchievement(achievementToDelete!!.id)
                    achievementToDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { achievementToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conquistas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, "Nova Conquista")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar conquista...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.achievements) { ach ->
                        ListItem(
                            headlineContent = { Text(ach.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${ach.rarity} • ${ach.xpReward} XP") },
                            leadingContent = {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                    AsyncImage(
                                        model = ach.iconUrl.toFullImageUrl(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = ach }) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { achievementToDelete = ach }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
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
fun AchievementFormDialog(
    achievement: Achievement?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, RarityType) -> Unit
) {
    var name by remember { mutableStateOf(achievement?.name ?: "") }
    var description by remember { mutableStateOf(achievement?.description ?: "") }
    var iconUrl by remember { mutableStateOf(achievement?.iconUrl ?: "") }
    var xpReward by remember { mutableStateOf(achievement?.xpReward?.toString() ?: "0") }
    var keyName by remember { mutableStateOf(achievement?.keyName ?: "") }
    var rarity by remember { mutableStateOf(achievement?.rarity ?: RarityType.COMMON) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (achievement == null) "Nova Conquista" else "Editar Conquista") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = iconUrl, onValueChange = { iconUrl = it }, label = "URL Ícone")
                QuestuaTextField(value = xpReward, onValueChange = { xpReward = it }, label = "XP")
                QuestuaTextField(value = keyName, onValueChange = { keyName = it }, label = "KeyName")

                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Raridade: ${rarity.name}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        RarityType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = { rarity = type; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(name, description, iconUrl, xpReward.toIntOrNull() ?: 0, keyName, rarity)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}