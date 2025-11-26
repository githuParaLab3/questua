package com.questua.app.presentation.languages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNewLanguage: () -> Unit,
    viewModel: LanguagesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meus Idiomas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate50,
                    titleContentColor = Slate900
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewLanguage,
                containerColor = Amber500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Novo Idioma", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Slate50
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingSpinner()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.userLanguages) { item ->
                        LanguageProgressCard(
                            item = item,
                            onResumeClick = { viewModel.resumeLanguage(item.userLanguage) },
                            onAbandonClick = { viewModel.abandonLanguage(item.userLanguage.id) }
                        )
                    }

                    // Espaço extra para o FAB não cobrir o último item
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            state.error?.let { error ->
                ErrorDialog(message = error, onDismiss = { viewModel.clearError() })
            }
        }
    }
}

@Composable
fun LanguageProgressCard(
    item: UserLanguageUi,
    onResumeClick: () -> Unit,
    onAbandonClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header do Card: Bandeira e Nome
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Bandeira
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    if (item.languageDetails?.iconUrl != null) {
                        QuestuaAsyncImage(
                            imageUrl = item.languageDetails.iconUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.languageDetails?.code ?: "??",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.languageDetails?.name ?: "Carregando...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "Iniciado em ${item.userLanguage.startedAt.take(10)}", // Formatação simples da data
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Estatísticas (Nível e XP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge XP
                StatBadge(
                    icon = Icons.Default.Bolt,
                    label = "${item.userLanguage.xpTotal} XP",
                    color = Amber500,
                    bgColor = Color(0xFFFFFBEB)
                )

                // Badge Nível
                StatBadge(
                    icon = Icons.Default.School,
                    label = "${item.userLanguage.cefrLevel} (Nv. ${item.userLanguage.gamificationLevel})",
                    color = QuestuaBlue,
                    bgColor = Color(0xFFEFF6FF)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Botão Desistir (Texto pequeno)
                TextButton(onClick = onAbandonClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Rose500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desistir", color = Rose500)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botão Retomar (Filled)
                Button(
                    onClick = onResumeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retomar")
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    bgColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        )
    }
}