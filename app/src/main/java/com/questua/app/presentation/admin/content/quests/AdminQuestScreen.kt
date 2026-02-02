package com.questua.app.presentation.admin.content.quests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
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
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAll() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        QuestFormDialog(
            quest = null,
            questPoints = state.questPoints,
            dialogues = state.dialogues,
            onDismiss = { isCreating = false },
            onConfirm = { title, qpId, dial, desc, diff, ord, xp, unl, foc, prem, ai, pub ->
                viewModel.saveQuest(null, qpId, dial, title, desc, diff, ord, xp, unl, foc, prem, ai, pub)
                isCreating = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quests", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) { Icon(Icons.Default.Add, "Nova Quest") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar quest...",
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.quests) { quest ->
                    val qpName = state.questPoints.find { it.id == quest.questPointId }?.title ?: "QP desconhecido"

                    ListItem(
                        modifier = Modifier.clickable { navController.navigate(Screen.AdminQuestDetail.passId(quest.id)) },
                        headlineContent = { Text(quest.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Em: $qpName • XP: ${quest.xpValue}") },
                        trailingContent = {
                            Badge(containerColor = if(quest.isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                                Text(if(quest.isPublished) "ATIVO" else "DRAFT")
                            }
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}