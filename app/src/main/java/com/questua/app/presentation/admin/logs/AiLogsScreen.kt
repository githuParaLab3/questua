package com.questua.app.presentation.admin.logs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.questua.app.domain.enums.AiGenerationStatus
import com.questua.app.domain.enums.AiTargetType
import com.questua.app.domain.model.AiGenerationLog
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import com.questua.app.presentation.navigation.Screen

val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLogsScreen(
    navController: NavController,
    viewModel: AdminAiLogsViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val logs = viewModel.filteredLogs
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Histórico de IA", fontWeight = FontWeight.Bold) },
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
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente de Fundo
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Barra de Busca e Filtros
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar prompt ou ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuestuaGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    val hasFilters = state.selectedStatus != null || state.selectedTarget != null
                    FilledTonalIconButton(
                        onClick = { showFilterSheet = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (hasFilters) QuestuaGold else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (hasFilters) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Box {
                            Icon(Icons.Default.Tune, contentDescription = "Filtros")
                            if (hasFilters) {
                                Badge(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp),
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (state.isLoading && state.logs.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = QuestuaGold
                        )
                    } else if (state.error != null && state.logs.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else if (logs.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nenhum registro encontrado.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(logs) { log ->
                                AiLogItem(
                                    log = log,
                                    onClick = { navController.navigate(Screen.AdminLogDetail.passId(log.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                FilterSheetContent(
                    selectedStatus = state.selectedStatus,
                    selectedTarget = state.selectedTarget,
                    onStatusSelected = viewModel::onStatusFilterSelected,
                    onTargetSelected = viewModel::onTargetFilterSelected,
                    onClear = {
                        viewModel.clearFilters()
                        showFilterSheet = false
                    },
                    onApply = { showFilterSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    selectedStatus: AiGenerationStatus?,
    selectedTarget: AiTargetType?,
    onStatusSelected: (AiGenerationStatus?) -> Unit,
    onTargetSelected: (AiTargetType?) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            "Filtrar Logs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            "Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiGenerationStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                    label = { Text(status.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuestuaGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Text(
            "Tipo de Conteúdo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiTargetType.entries.forEach { target ->
                FilterChip(
                    selected = selectedTarget == target,
                    onClick = { onTargetSelected(if (selectedTarget == target) null else target) },
                    label = { Text(target.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = QuestuaGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Limpar", color = MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestuaGold,
                    contentColor = Color.Black
                )
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AiLogItem(log: AiGenerationLog, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        ListItem(
            headlineContent = {
                Text(
                    log.targetType.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        log.prompt,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        log.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            leadingContent = {
                Surface(
                    shape = CircleShape,
                    color = QuestuaGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QuestuaGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            trailingContent = { StatusBadge(status = log.status) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
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
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
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