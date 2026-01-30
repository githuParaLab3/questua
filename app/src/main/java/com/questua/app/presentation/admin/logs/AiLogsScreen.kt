package com.questua.app.presentation.admin.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.model.AiGenerationLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLogsScreen(
    navController: NavController,
    viewModel: AdminAiLogsViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Geração IA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.logs.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.logs.isEmpty()) {
                Text(
                    text = state.error ?: "Erro desconhecido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.logs) { log ->
                        AiLogItem(log = log)
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
fun AiLogItem(log: AiGenerationLog) {
    ListItem(
        headlineContent = {
            Text(
                text = "Alvo: ${log.targetType}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = "Prompt: ${log.prompt}", // Corrigido de promptUsed para prompt
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: ${log.createdAt}",
                    style = MaterialTheme.typography.labelSmall, // labelExtraSmall não existe no M3 padrão
                    color = Color.Gray
                )
            }
        },
        trailingContent = {
            StatusBadge(status = log.status)
        }
    )
}

@Composable
fun StatusBadge(status: AiGenerationStatus) {
    val color = when (status) {
        AiGenerationStatus.SUCCESS -> Color(0xFF4CAF50)
        AiGenerationStatus.ERROR -> MaterialTheme.colorScheme.error
        AiGenerationStatus.TIMEOUT -> Color(0xFFFF9800)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}