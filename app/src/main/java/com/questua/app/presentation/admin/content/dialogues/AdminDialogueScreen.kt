package com.questua.app.presentation.admin.content.dialogues

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
fun AdminDialogueScreen(
    navController: NavController,
    viewModel: AdminDialogueViewModel = hiltViewModel()
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
        SceneDialogueFormDialog(
            dialogue = null,
            characters = state.characters,
            allDialogues = state.dialogues,
            onDismiss = { isCreating = false },
            onConfirm = { txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai ->
                viewModel.saveDialogue(txt, desc, bg, mus, st, eff, spk, aud, exp, mod, er, ch, nxt, ai)
                isCreating = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diálogos", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) { Icon(Icons.Default.Add, "Novo Diálogo") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar diálogo...",
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.dialogues) { dialog ->
                    val speakerName = state.characters.find { it.id == dialog.speakerCharacterId }?.name ?: "Narrador"

                    ListItem(
                        modifier = Modifier.clickable { navController.navigate(Screen.AdminDialogueDetail.passId(dialog.id)) },
                        headlineContent = { Text(dialog.textContent, maxLines = 1, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("$speakerName • ${dialog.description}") },
                        leadingContent = { Icon(Icons.Default.Chat, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}