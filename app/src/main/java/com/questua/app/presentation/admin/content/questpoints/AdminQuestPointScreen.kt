package com.questua.app.presentation.admin.content.questpoints

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
import com.questua.app.domain.model.QuestPoint
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestPointScreen(
    navController: NavController,
    viewModel: AdminQuestPointViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchPoints()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        QuestPointFormDialog(
            questPoint = null,
            onDismiss = { isCreating = false },
            onConfirm = { name, cityId, lat, lon, desc, _ ->
                // CORREÇÃO: Passando explicitamente para os argumentos corretos do ViewModel
                viewModel.savePoint(
                    id = null,
                    cityId = cityId,
                    title = name, // 'name' da UI vai para 'title' do ViewModel/Model
                    desc = desc,
                    lat = lat,
                    lon = lon
                )
                isCreating = false
            }
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

            // Exibição de erro para debug se a criação falhar
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.points) { point ->
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.AdminQuestPointDetail.passId(point.id))
                            },
                            headlineContent = { Text(point.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Lat: ${point.lat}, Lon: ${point.lon}") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Place,
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
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestPointFormDialog(
    questPoint: QuestPoint?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, String, String?) -> Unit
) {
    // CORREÇÃO: No model QuestPoint o campo é 'title', não 'name'
    var name by remember { mutableStateOf(questPoint?.title ?: "") }
    var cityId by remember { mutableStateOf(questPoint?.cityId ?: "") }
    var lat by remember { mutableStateOf(questPoint?.lat?.toString() ?: "") }
    var lon by remember { mutableStateOf(questPoint?.lon?.toString() ?: "") }
    var desc by remember { mutableStateOf(questPoint?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (questPoint == null) "Novo Ponto" else "Editar Ponto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome/Título")
                QuestuaTextField(value = cityId, onValueChange = { cityId = it }, label = "ID da Cidade (UUID)")
                QuestuaTextField(value = lat, onValueChange = { lat = it }, label = "Latitude")
                QuestuaTextField(value = lon, onValueChange = { lon = it }, label = "Longitude")
                QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        cityId,
                        lat.toDoubleOrNull() ?: 0.0,
                        lon.toDoubleOrNull() ?: 0.0,
                        desc,
                        null
                    )
                },
                enabled = name.isNotBlank() && cityId.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}