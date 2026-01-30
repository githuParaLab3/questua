package com.questua.app.presentation.admin.content.cities

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
import com.questua.app.domain.model.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCityScreen(
    navController: NavController,
    viewModel: AdminCityViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFormDialog by remember { mutableStateOf<City?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var cityToDelete by remember { mutableStateOf<City?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchCities()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating || showFormDialog != null) {
        CityFormDialog(
            city = showFormDialog,
            onDismiss = { isCreating = false; showFormDialog = null },
            onConfirm = { name, code, desc, langId, lat, lon, url ->
                viewModel.saveCity(showFormDialog?.id, name, code, desc, langId, lat, lon, url)
                isCreating = false
                showFormDialog = null
            }
        )
    }

    if (cityToDelete != null) {
        AlertDialog(
            onDismissRequest = { cityToDelete = null },
            title = { Text("Excluir Cidade") },
            text = { Text("Deseja excluir '${cityToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCity(cityToDelete!!.id)
                    cityToDelete = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { cityToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cidades", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) {
                Icon(Icons.Default.Add, "Nova Cidade")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar cidade...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.cities) { city ->
                        ListItem(
                            headlineContent = { Text(city.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${city.countryCode} • ${city.lat}, ${city.lon}") },
                            leadingContent = { Icon(Icons.Default.LocationCity, tint = MaterialTheme.colorScheme.primary, contentDescription = null) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showFormDialog = city }) {
                                        Icon(Icons.Default.Edit, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                                    }
                                    IconButton(onClick = { cityToDelete = city }) {
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
fun CityFormDialog(
    city: City?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Double, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf(city?.name ?: "") }
    var countryCode by remember { mutableStateOf(city?.countryCode ?: "") }
    var description by remember { mutableStateOf(city?.description ?: "") }
    var languageId by remember { mutableStateOf(city?.languageId ?: "") }
    var lat by remember { mutableStateOf(city?.lat?.toString() ?: "0.0") }
    var lon by remember { mutableStateOf(city?.lon?.toString() ?: "0.0") }
    var imageUrl by remember { mutableStateOf(city?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (city == null) "Nova Cidade" else "Editar Cidade") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                QuestuaTextField(value = countryCode, onValueChange = { countryCode = it }, label = "Código País (Ex: BR)")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = languageId, onValueChange = { languageId = it }, label = "ID do Idioma")
                QuestuaTextField(value = lat, onValueChange = { lat = it }, label = "Latitude")
                QuestuaTextField(value = lon, onValueChange = { lon = it }, label = "Longitude")
                QuestuaTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = "URL Imagem")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    name,
                    countryCode,
                    description,
                    languageId,
                    lat.toDoubleOrNull() ?: 0.0,
                    lon.toDoubleOrNull() ?: 0.0,
                    imageUrl.takeIf { it.isNotBlank() }
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}