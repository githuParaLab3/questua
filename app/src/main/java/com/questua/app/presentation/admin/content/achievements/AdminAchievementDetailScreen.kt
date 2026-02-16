package com.questua.app.presentation.admin.content.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.enums.RarityType
import com.questua.app.presentation.admin.content.cities.DetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAchievementDetailScreen(
    navController: NavController,
    viewModel: AdminAchievementDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.achievement != null) {
        AchievementFormDialog(
            achievement = state.achievement,
            onDismiss = { showEdit = false },
            onConfirm = { key, name, desc, icon, rar, xp, meta ->
                viewModel.saveAchievement(key, name, desc, icon, rar, xp, meta)
                showEdit = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.achievement?.name ?: "Detalhes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            if (state.achievement != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EXCLUIR", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showEdit = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuestuaGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EDITAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                QuestuaGold.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            if (state.achievement != null) {
                val rarityColor = when (state.achievement.rarity) {
                    RarityType.LEGENDARY -> Color(0xFFFFD700)
                    RarityType.EPIC -> Color(0xFF9C27B0)
                    RarityType.RARE -> Color(0xFF2196F3)
                    else -> Color.Gray
                }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ícone Destaque
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(24.dp))
                            .background(rarityColor.copy(alpha = 0.1f))
                            .border(2.dp, rarityColor, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.achievement.iconUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = state.achievement.iconUrl.toFullImageUrl(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.padding(16.dp).fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = rarityColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    DetailCard("Principal", listOf(
                        "Nome" to state.achievement.name,
                        "Chave" to state.achievement.keyName,
                        "Raridade" to state.achievement.rarity.name,
                        "XP" to state.achievement.xpReward.toString()
                    ))

                    DetailCard("Visual", listOf(
                        "Ícone URL" to (state.achievement.iconUrl ?: "N/A"),
                        "Descrição" to (state.achievement.description ?: "N/A")
                    ))

                    state.achievement.metadata?.let { meta ->
                        DetailCard("Metadados", listOf(
                            "Categoria" to (meta.category ?: "-"),
                            "Info Extra" to (meta.descriptionExtra ?: "-")
                        ))
                    }
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Conquista", fontWeight = FontWeight.Bold) },
                text = { Text("Tem certeza? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteAchievement(); showDelete = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDelete = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Cancelar")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}