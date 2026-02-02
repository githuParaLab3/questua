// fileName: app/src/main/java/com/questua/app/presentation/admin/content/questpoints/AdminQuestPointScreen.kt
package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.foundation.clickable
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
fun AdminQuestPointScreen(
    navController: NavController,
    viewModel: AdminQuestPointViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAll()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        QuestPointFormDialog(
            questPoint = null,
            cities = state.cities,
            onDismiss = { isCreating = false },
            onConfirm = { title, cId, desc, diff, lat, lon, img, ico, unl, prem, ai, pub ->
                viewModel.savePoint(null, cId, title, desc, diff, lat, lon, img, ico, unl, prem, ai, pub)
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

            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.points) { point ->
                    val cityName = state.cities.find { it.id == point.cityId }?.name ?: point.cityId.take(8)

                    ListItem(
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.AdminQuestPointDetail.passId(point.id))
                        },
                        headlineContent = { Text(point.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("$cityName • Dif: ${point.difficulty}") },
                        leadingContent = {
                            Icon(if(point.isPremium) Icons.Default.Star else Icons.Default.Place,
                                tint = if(point.isPremium) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                contentDescription = null)
                        },
                        trailingContent = {
                            Badge(containerColor = if(point.isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                                Text(if(point.isPublished) "ATIVO" else "DRAFT")
                            }
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}