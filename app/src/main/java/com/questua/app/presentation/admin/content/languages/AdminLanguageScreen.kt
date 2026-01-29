package com.questua.app.presentation.admin.content.languages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.Language
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLanguageScreen(
    navController: NavController,
    viewModel: AdminLanguageViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<Language?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var languageToDelete by remember { mutableStateOf<Language?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchLanguages()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        LanguageFormDialog(
            language = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { name, code ->
                viewModel.saveLanguage(showFormDialog?.id, name, code)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (languageToDelete != null) {
        AlertDialog(
            onDismissRequest = { languageToDelete = null },
            title = { Text("Excluir Idioma") },
            text = { Text("Tem certeza que deseja excluir '${languageToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLanguage(languageToDelete!!.id); languageToDelete = null }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { languageToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Idiomas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar idioma...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.languages) { lang ->
                        ListItem(
                            headlineContent = { Text(lang.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(lang.code.uppercase()) },
                            leadingContent = { Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = lang }) {
                                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { languageToDelete = lang }) {
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
fun LanguageFormDialog(language: Language?, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(language?.name ?: "") }
    var code by remember { mutableStateOf(language?.code ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == null) "Novo Idioma" else "Editar Idioma") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = code, onValueChange = { code = it }, label = "Código")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, code) }, enabled = name.isNotBlank() && code.isNotBlank()) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}