package com.questua.app.presentation.admin.feedback

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.model.Report
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminFeedbackScreen(
    navController: NavController,
    viewModel: AdminFeedbackViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Filtra e prepara as listas na UI
    val openReports = remember(state.reports) {
        state.reports.filter { it.status == ReportStatus.OPEN }
            .sortedByDescending { it.createdAt }
    }
    val resolvedReports = remember(state.reports) {
        state.reports.filter { it.status == ReportStatus.RESOLVED }
            .sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Reports") },
                actions = {
                    IconButton(onClick = { viewModel.loadReports() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading && state.reports.isEmpty()) {
                LoadingSpinner()
            } else {
                if (state.reports.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- SEÇÃO: EM ABERTO ---
                        if (openReports.isNotEmpty()) {
                            stickyHeader {
                                SectionHeader(title = "Em Aberto", count = openReports.size, color = MaterialTheme.colorScheme.error)
                            }

                            items(
                                items = openReports,
                                key = { it.id } // Importante para performance e animações
                            ) { report ->
                                ReportItem(
                                    report = report,
                                    onResolve = { viewModel.resolveReport(report) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // --- SEÇÃO: RESOLVIDOS ---
                        if (resolvedReports.isNotEmpty()) {
                            stickyHeader {
                                SectionHeader(title = "Resolvidos", count = resolvedReports.size, color = MaterialTheme.colorScheme.primary)
                            }

                            items(
                                items = resolvedReports,
                                key = { it.id }
                            ) { report ->
                                ReportItem(
                                    report = report,
                                    onResolve = { viewModel.resolveReport(report) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let {
                ErrorDialog(message = it, onDismiss = { viewModel.loadReports() })
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.background, // Mesma cor do fundo para parecer transparente mas cobrir o scroll
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Badge(
                containerColor = color.copy(alpha = 0.1f),
                contentColor = color
            ) {
                Text(text = count.toString(), modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tudo limpo por aqui!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportItem(
    report: Report,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isResolved = report.status == ReportStatus.RESOLVED
    val cardColor = if (isResolved) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isResolved) 0.dp else 2.dp),
        border = if(isResolved) null else null // Opcional: borda para abertos
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = report.type.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.createdAt.take(10),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(isResolved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isResolved) {
                    IconButton(
                        onClick = onResolve,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Resolver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (report.userId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: ${report.userId.take(8)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}