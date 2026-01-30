package com.questua.app.presentation.admin.content.characters

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
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Persona
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCharacterScreen(
    navController: NavController,
    viewModel: AdminCharacterViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchCharacters()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        CharacterFormDialog(
            character = null,
            onDismiss = { isCreating = false },
            onConfirm = { name, url, isAi, voice, persona ->
                viewModel.saveCharacter(null, name, url, isAi, voice, persona)
                isCreating = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personagens", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, "Novo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar personagem...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.characters) { char ->
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.AdminCharacterDetail.passId(char.id))
                            },
                            headlineContent = { Text(char.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(if (char.isAiGenerated) "IA Generated" else "Manual") },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterFormDialog(
    character: CharacterEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String?, Persona?) -> Unit
) {
    var name by remember { mutableStateOf(character?.name ?: "") }
    var url by remember { mutableStateOf(character?.avatarUrl ?: "") }
    var isAi by remember { mutableStateOf(character?.isAiGenerated ?: false) }
    var voice by remember { mutableStateOf(character?.voiceUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (character == null) "Novo Personagem" else "Editar") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = url, onValueChange = { url = it }, label = "URL Avatar")
                QuestuaTextField(value = voice, onValueChange = { voice = it }, label = "URL Voz (Opcional)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAi, onCheckedChange = { isAi = it })
                    Text("Gerado por IA")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, url, isAi, voice.takeIf { it.isNotBlank() }, character?.persona) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}