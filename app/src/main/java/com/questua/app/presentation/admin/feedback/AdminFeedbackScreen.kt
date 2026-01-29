package com.questua.app.presentation.admin.feedback

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.ReportStatus
import com.questua.app.domain.enums.ReportType
import com.questua.app.domain.model.Report
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import com.questua.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminFeedbackScreen(
    navController: NavController,
    viewModel: AdminFeedbackViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val currentBackStackEntry = navController.currentBackStackEntry

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadReports()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Feedbacks", fontWeight = FontWeight.Bold) })
        },
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = "Descrição ou ID...",
                    label = null,
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Close, null) } }
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                val hasFilters = state.selectedStatusFilter != null || state.selectedTypeFilter != null
                FilledTonalIconButton(
                    onClick = { showFilterSheet = true },
                    colors = if (hasFilters) IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Box {
                        Icon(Icons.Default.Tune, null)
                        if (hasFilters) Badge(modifier = Modifier.size(8.dp).align(Alignment.TopEnd), containerColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.reports.isEmpty()) {
                    LoadingSpinner()
                } else if (state.reports.isEmpty()) {
                    EmptyState(isSearching = true)
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(state.reports) { report ->
                            ReportItem(report = report, onClick = { navController.navigate(Screen.AdminReportDetail.passId(report.id)) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, sheetState = sheetState) {
            FeedbackFilterSheetContent(
                state = state,
                onStatusSelected = viewModel::onStatusFilterChange,
                onTypeSelected = viewModel::onTypeFilterChange,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackFilterSheetContent(
    state: AdminFeedbackState,
    onStatusSelected: (ReportStatus?) -> Unit,
    onTypeSelected: (ReportType?) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 48.dp).fillMaxWidth()) {
        Text("Filtros", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = state.selectedStatusFilter == null, onClick = { onStatusSelected(null) }, label = { Text("Todos") }) }
            items(ReportStatus.entries) { status ->
                FilterChip(selected = state.selectedStatusFilter == status, onClick = { onStatusSelected(status) }, label = { Text(status.name) })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tipo de Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = state.selectedTypeFilter == null, onClick = { onTypeSelected(null) }, label = { Text("Todos") }) }
            items(ReportType.entries) { type ->
                FilterChip(selected = state.selectedTypeFilter == type, onClick = { onTypeSelected(type) }, label = { Text(type.name) })
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { onStatusSelected(null); onTypeSelected(null) }, modifier = Modifier.weight(1f)) { Text("Limpar") }
            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Ver Resultados") }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    val isResolved = report.status == ReportStatus.RESOLVED
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(report.description, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(report.createdAt.take(10), style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            val color = if (isResolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Icon(if (isResolved) Icons.Default.CheckCircle else Icons.Default.Info, null, tint = color)
        },
        trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
    )
}

@Composable
fun EmptyState(isSearching: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhum feedback encontrado.", color = Color.Gray)
    }
}