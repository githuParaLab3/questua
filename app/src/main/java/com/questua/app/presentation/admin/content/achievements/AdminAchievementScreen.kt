package com.questua.app.presentation.admin.content.achievements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.questua.app.domain.enums.RarityType
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAchievementScreen(
    navController: NavController,
    viewModel: AdminAchievementViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCreating by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.fetchAchievements() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isCreating) {
        AchievementFormDialog(
            achievement = null,
            onDismiss = { isCreating = false },
            onConfirm = { key, name, desc, icon, rar, xp, meta ->
                viewModel.saveAchievement(null, key, name, desc, icon, rar, xp, meta)
                isCreating = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conquistas", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreating = true }) { Icon(Icons.Default.Add, "Nova") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            QuestuaTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Pesquisar...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.achievements) { item ->
                    ListItem(
                        modifier = Modifier.clickable { navController.navigate(Screen.AdminAchievementDetail.passId(item.id)) },
                        headlineContent = { Text(item.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("XP: ${item.xpReward} • ${item.rarity.name}") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Star, null,
                                tint = when(item.rarity) {
                                    RarityType.LEGENDARY -> Color(0xFFFFD700)
                                    RarityType.EPIC -> Color(0xFF9C27B0)
                                    RarityType.RARE -> Color(0xFF2196F3)
                                    else -> Color.Gray
                                }
                            )
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}