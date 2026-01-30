package com.questua.app.presentation.admin.content.questpoints

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
import com.questua.app.domain.model.QuestPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestPointScreen(
    navController: NavController,
    viewModel: AdminQuestPointViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<QuestPoint?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var pointToDelete by remember { mutableStateOf<QuestPoint?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchPoints()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        QuestPointFormDialog(
            point = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { cityId, title, desc, lat, lon ->
                viewModel.savePoint(showFormDialog?.id, cityId, title, desc, lat, lon)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (pointToDelete != null) {
        AlertDialog(
            onDismissRequest = { pointToDelete = null },
            title = { Text("Excluir Ponto") },
            text = { Text("Deseja excluir '${pointToDelete?.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePoint(pointToDelete!!.id)
                    pointToDelete = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pointToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quest Points", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, "Novo Ponto")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar ponto...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.points) { point ->
                        ListItem(
                            headlineContent = { Text(point.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${point.lat}, ${point.lon}") },
                            leadingContent = { Icon(Icons.Default.Place, tint = MaterialTheme.colorScheme.primary, contentDescription = null) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = point }) {
                                        Icon(Icons.Default.Edit, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                                    }
                                    IconButton(onClick = { pointToDelete = point }) {
                                        Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null)
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
fun QuestPointFormDialog(
    point: QuestPoint?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Double) -> Unit
) {
    var cityId by remember { mutableStateOf(point?.cityId ?: "") }
    var title by remember { mutableStateOf(point?.title ?: "") }
    var description by remember { mutableStateOf(point?.description ?: "") } // Corrigido para 'description' do modelo de domínio
    var lat by remember { mutableStateOf(point?.lat?.toString() ?: "0.0") }
    var lon by remember { mutableStateOf(point?.lon?.toString() ?: "0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (point == null) "Novo Ponto" else "Editar Ponto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = cityId, onValueChange = { cityId = it }, label = "ID da Cidade")
                QuestuaTextField(value = title, onValueChange = { title = it }, label = "Título")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = lat, onValueChange = { lat = it }, label = "Latitude")
                QuestuaTextField(value = lon, onValueChange = { lon = it }, label = "Longitude")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(cityId, title, description, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}