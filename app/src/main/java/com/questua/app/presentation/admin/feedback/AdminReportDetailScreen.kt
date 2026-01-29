package com.questua.app.presentation.admin.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.domain.enums.ReportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportDetailScreen(
    navController: NavController,
    viewModel: AdminReportDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            navController.previousBackStackEntry?.savedStateHandle?.set("feedback_success_message", message)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Feedback", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                LoadingSpinner()
            } else {
                state.report?.let { report ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = if (report.status == ReportStatus.RESOLVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    Badge(containerColor = statusColor) {
                                        Text(report.status.name, modifier = Modifier.padding(4.dp))
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(report.createdAt.take(10), style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("DESCRIÇÃO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(report.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        if (!report.screenshotUrl.isNullOrBlank()) {
                            Text("ANEXO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(report.screenshotUrl.toFullImageUrl())
                                    .crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.05f)),
                                contentScale = ContentScale.Fit
                            )
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("DADOS TÉCNICOS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                DetailItem("Tipo", report.type.name)
                                DetailItem("User ID", report.userId.take(12) + "...")
                                report.deviceInfo?.let {
                                    DetailItem("Dispositivo", it.deviceModel ?: "N/A")
                                    DetailItem("Android", it.androidVersion ?: "N/A")
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (report.status == ReportStatus.OPEN) {
                                QuestuaButton(
                                    text = "Resolver",
                                    onClick = { viewModel.resolveReport() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Button(
                                onClick = { viewModel.deleteReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Excluir")
                            }
                        }
                    }
                }
            }
        }
        state.error?.let { ErrorDialog(message = it, onDismiss = { viewModel.clearError() }) }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}