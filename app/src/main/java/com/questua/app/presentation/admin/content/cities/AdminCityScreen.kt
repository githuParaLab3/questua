package com.questua.app.presentation.admin.content.cities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCityScreen(navController: NavController, viewModel: AdminCityViewModel = hiltViewModel()) {
    val state = viewModel.state
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Conteúdo: Cidades") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = "Pesquisar cidades...",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                trailingIcon = { Icon(Icons.Default.Search, null) }
            )
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.cities) { city ->
                    ListItem(
                        headlineContent = { Text(city.name) },
                        supportingContent = { Text("${city.countryCode} • ${city.id}") },
                        trailingContent = {
                            Badge(containerColor = if(city.isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                                Text(if(city.isPublished) "ATIVO" else "DRAFT")
                            }
                        },
                        modifier = Modifier.clickable { navController.navigate(Screen.AdminCityDetail.route.replace("{cityId}", city.id)) }
                    )
                    HorizontalDivider()
                }
            }
        }
        if (showCreate) {
            CityFormDialog(languages = state.languages, onDismiss = { showCreate = false }, onConfirm = { n, c, d, l, p, la, lo, img, ico, pre, unl, ai, pub ->
                viewModel.createCity(n, c, d, l, p, la, lo, img, ico, pre, unl, ai, pub)
                showCreate = false
            })
        }
    }
}