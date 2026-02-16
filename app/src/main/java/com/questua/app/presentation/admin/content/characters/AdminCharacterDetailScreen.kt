package com.questua.app.presentation.admin.content.characters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import com.questua.app.presentation.admin.content.cities.DetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCharacterDetailScreen(
    navController: NavController,
    viewModel: AdminCharacterDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navController.popBackStack() }

    if (showEdit && state.character != null) {
        CharacterFormDialog(
            character = state.character,
            onDismiss = { showEdit = false },
            onConfirm = { name, av, vc, sp, per, ai ->
                viewModel.saveCharacter(name, av, vc, sp, per, ai)
                showEdit = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.character?.name ?: "Detalhes", fontWeight = FontWeight.Bold) },
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
            if (state.character != null) {
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

            if (state.character != null) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar Destaque
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, QuestuaGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.character.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = state.character.avatarUrl.toFullImageUrl(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    DetailCard("Info Básica", listOf(
                        "Nome" to state.character.name,
                        "ID" to state.character.id,
                        "Gerado por IA" to if(state.character.isAiGenerated) "Sim" else "Não",
                        "Criado em" to state.character.createdAt
                    ))

                    DetailCard("Mídia", listOf(
                        "Avatar URL" to (state.character.avatarUrl ?: "Não possui"),
                        "Voz URL" to (state.character.voiceUrl ?: "Não possui"),
                        "Sprites" to "${state.character.spriteSheet?.urls?.size ?: 0} imagens"
                    ))

                    state.character.persona?.let { p ->
                        DetailCard("Persona: Descrição", listOf("" to (p.description ?: "Sem descrição")))

                        DetailCard("Persona: Atributos", listOf(
                            "Estilo de Fala" to (p.speakingStyle ?: "-"),
                            "Tom de Voz" to (p.voiceTone ?: "-"),
                            "Background" to (p.background ?: "-")
                        ))

                        if (p.traits.isNotEmpty()) {
                            DetailCard("Persona: Traços", p.traits.mapIndexed { i, t -> "Traço ${i+1}" to t })
                        }
                    }
                }
            }
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Excluir Personagem", fontWeight = FontWeight.Bold) },
                text = { Text("Tem certeza? Esta ação é irreversível.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteCharacter(); showDelete = false },
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