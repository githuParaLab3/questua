package com.questua.app.presentation.admin.content.quests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.Quest
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestScreen(
    navController: NavController,
    viewModel: AdminQuestViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchQuests()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        QuestFormDialog(
            quest = null,
            onDismiss = { isCreating = false },
            onConfirm = { pId, t, d, diff, ord, xp, prem, pub ->
                viewModel.saveQuest(null, pId, t, d, diff, ord, xp, prem, pub)
                isCreating = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Quests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Quest")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar quest...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.quests) { quest ->
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.AdminQuestDetail.passId(quest.id))
                            },
                            headlineContent = { Text(quest.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Text("Ordem: ${quest.orderIndex} • XP: ${quest.xpValue} • Dif: ${quest.difficulty}")
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Assignment, // Ou Icons.Default.Assignment dependendo da disponibilidade
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    tint = MaterialTheme.colorScheme.outline,
                                    contentDescription = null
                                )
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
fun QuestFormDialog(
    quest: Quest?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Int, Int, Boolean, Boolean) -> Unit
) {
    var pId by remember { mutableStateOf(quest?.questPointId ?: "") }
    var t by remember { mutableStateOf(quest?.title ?: "") }
    var d by remember { mutableStateOf(quest?.description ?: "") }
    var diff by remember { mutableStateOf(quest?.difficulty?.toString() ?: "1") }
    var ord by remember { mutableStateOf(quest?.orderIndex?.toString() ?: "1") }
    var xp by remember { mutableStateOf(quest?.xpValue?.toString() ?: "0") }
    var prem by remember { mutableStateOf(quest?.isPremium ?: false) }
    var pub by remember { mutableStateOf(quest?.isPublished ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quest == null) "Nova Quest" else "Editar Quest") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = pId, onValueChange = { pId = it }, label = "ID Quest Point")
                QuestuaTextField(value = t, onValueChange = { t = it }, label = "Título")
                QuestuaTextField(value = d, onValueChange = { d = it }, label = "Descrição")
                QuestuaTextField(value = diff, onValueChange = { diff = it }, label = "Dificuldade (1-5)")
                QuestuaTextField(value = ord, onValueChange = { ord = it }, label = "Ordem")
                QuestuaTextField(value = xp, onValueChange = { xp = it }, label = "XP")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = prem, onCheckedChange = { prem = it })
                    Text("Premium")
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = pub, onCheckedChange = { pub = it })
                    Text("Publicada")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        pId, t, d,
                        diff.toIntOrNull() ?: 1,
                        ord.toIntOrNull() ?: 1,
                        xp.toIntOrNull() ?: 0,
                        prem, pub
                    )
                },
                enabled = pId.isNotBlank() && t.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}