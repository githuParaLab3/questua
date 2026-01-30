package com.questua.app.presentation.admin.content.cities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCityDetailScreen(
    navController: NavController,
    viewModel: AdminCityDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            navController.popBackStack()
        }
    }

    if (showEditDialog && state.city != null) {
        CityFormDialog(
            city = state.city,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, code, desc, langId, lat, lon, url ->
                viewModel.saveCity(
                    id = state.city.id,
                    name = name,
                    code = code,
                    desc = desc,
                    langId = langId,
                    lat = lat,
                    lon = lon,
                    url = url
                )
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Cidade") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (state.city != null && !state.isLoading) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Excluir")
                        }
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Editar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
            } else {
                state.city?.let { city ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CityInfoCard("Identificação e Localização", listOf(
                            "ID" to city.id,
                            "Nome" to city.name,
                            "Código do País" to city.countryCode,
                            "Latitude" to city.lat.toString(),
                            "Longitude" to city.lon.toString()
                        ))

                        CityInfoCard("Conteúdo e Status", listOf(
                            "Descrição" to city.description,
                            "Idioma ID" to city.languageId,
                            "Premium" to if (city.isPremium) "Sim" else "Não",
                            "Publicado" to if (city.isPublished) "Sim" else "Não",
                            "Gerado por IA" to if (city.isAiGenerated) "Sim" else "Não",
                            "Criado em" to city.createdAt
                        ))

                        if (city.imageUrl != null || city.iconUrl != null) {
                            CityInfoCard("Média", listOf(
                                "Imagem URL" to (city.imageUrl ?: "N/A"),
                                "Ícone URL" to (city.iconUrl ?: "N/A")
                            ))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza que deseja excluir esta cidade? Esta ação é irreversível.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCity()
                        showDeleteDialog = false
                    }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
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

@Composable
private fun CityInfoCard(title: String, items: List<Pair<String, String>>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            items.forEach { (label, value) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}