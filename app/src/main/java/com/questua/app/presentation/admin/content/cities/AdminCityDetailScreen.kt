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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCityDetailScreen(navController: NavController, viewModel: AdminCityDetailViewModel = hiltViewModel()) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(state.city?.name ?: "Detalhes") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            })
        },
        bottomBar = {
            state.city?.let {
                Surface(tonalElevation = 8.dp, shadowElevation = 10.dp) {
                    Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = { viewModel.deleteCity() }, Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, null); Text(" EXCLUIR")
                        }
                        Button(onClick = { showEdit = true }, Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null); Text(" EDITAR")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.city != null) {
            Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                DetailCard("Identificação", listOf("ID" to state.city.id, "Nome" to state.city.name, "Criado em" to state.city.createdAt))
                DetailCard("Geografia e Idioma", listOf("Código País" to state.city.countryCode, "Idioma ID" to state.city.languageId, "Latitude" to state.city.lat.toString(), "Longitude" to state.city.lon.toString()))
                DetailCard("Status e Acesso", listOf("Publicado" to if(state.city.isPublished) "Sim" else "Não", "Premium" to if(state.city.isPremium) "Sim" else "Não", "Gerado por IA" to if(state.city.isAiGenerated) "Sim" else "Não"))
                DetailCard("Media", listOf("Imagem URL" to (state.city.imageUrl ?: "N/A"), "Ícone URL" to (state.city.iconUrl ?: "N/A")))
                DetailCard("Descrição", listOf("" to state.city.description))
            }
        }
        if (showEdit && state.city != null) {
            CityFormDialog(city = state.city, languages = state.languages, onDismiss = { showEdit = false }, onConfirm = { n, c, d, l, p, la, lo, img, ico, pre, unl, ai, pub ->
                viewModel.updateCity(n, c, d, l, p, la, lo, img, ico, pre, unl, ai, pub)
                showEdit = false
            })
        }
    }
}

@Composable
fun DetailCard(title: String, items: List<Pair<String, String>>) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            items.forEach { (label, value) ->
                if(label.isNotEmpty()) Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}