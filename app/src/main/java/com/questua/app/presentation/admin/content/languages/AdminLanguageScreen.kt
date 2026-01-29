package com.questua.app.presentation.admin.content.languages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.common.uriToFile
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.Language
import java.io.File

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
            onConfirm = { name, code, imageFile ->
                viewModel.saveLanguage(showFormDialog?.id, name, code, imageFile)
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
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Idioma")
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
                            leadingContent = {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    if (lang.iconUrl != null) {
                                        AsyncImage(
                                            model = lang.iconUrl.toFullImageUrl(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
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
fun LanguageFormDialog(
    language: Language?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, File?) -> Unit
) {
    var name by remember { mutableStateOf(language?.name ?: "") }
    var code by remember { mutableStateOf(language?.code ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == null) "Novo Idioma" else "Editar Idioma") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (language?.iconUrl != null) {
                        AsyncImage(
                            model = language.iconUrl.toFullImageUrl(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(32.dp))
                    }
                }

                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = code, onValueChange = { code = it }, label = "Código")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val file = selectedImageUri?.let { context.uriToFile(it) }
                    onConfirm(name, code, file)
                },
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}