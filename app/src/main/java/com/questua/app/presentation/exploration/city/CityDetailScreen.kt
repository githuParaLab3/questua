package com.questua.app.presentation.exploration.city

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.theme.Amber500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestPoint: (String) -> Unit,
    viewModel: CityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.city?.name ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            // Botão Inferior Dinâmico
            if (!state.isLoading && state.city != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 16.dp) // Safety margin
                    ) {
                        QuestuaButton(
                            text = if (state.hasActiveProgress && state.suggestedPoint != null) {
                                "Continuar ${state.suggestedPoint?.title}"
                            } else {
                                "Começar Exploração"
                            },
                            onClick = {
                                state.suggestedPoint?.let { point ->
                                    onNavigateToQuestPoint(point.id)
                                }
                            },
                            leadingIcon = if (state.hasActiveProgress) Icons.Default.PlayArrow else Icons.Default.Map,
                            enabled = state.suggestedPoint != null
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingSpinner()
            } else {
                state.city?.let { city ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            // Padding extra embaixo para o conteúdo não ficar escondido atrás da BottomBar
                            .padding(bottom = 100.dp)
                    ) {
                        // Header com Imagem
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(city.imageUrl?.toFullImageUrl())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagem de ${city.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                            startY = 200f
                                        )
                                    )
                            )

                            Text(
                                text = "Bem-vindo a ${city.name}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )
                        }

                        // Descrição
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sobre a cidade",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = city.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Mapa Interativo
                        Text(
                            text = "Mapa de Missões",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp) // Aumentei um pouco a altura para melhor interação
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            val cityLocation = LatLng(city.lat, city.lon)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(cityLocation, 12f)
                            }

                            val mapProperties = remember {
                                MapProperties(
                                    mapStyleOptions = try {
                                        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                                    } catch (e: Exception) { null },
                                    isMyLocationEnabled = false
                                )
                            }

                            val mapUiSettings = remember {
                                MapUiSettings(
                                    zoomControlsEnabled = true,
                                    scrollGesturesEnabled = true,
                                    rotationGesturesEnabled = false,
                                    mapToolbarEnabled = false
                                )
                            }

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = mapProperties,
                                uiSettings = mapUiSettings
                            ) {
                                state.questPoints.forEach { point ->
                                    MarkerInfoWindow(
                                        state = MarkerState(position = LatLng(point.lat, point.lon)),
                                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                                        onInfoWindowClick = {
                                            onNavigateToQuestPoint(point.id)
                                        }
                                    ) {
                                        // Custom Info Window Content
                                        Card(
                                            modifier = Modifier
                                                .width(200.dp)
                                                .wrapContentHeight(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = point.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Nível de Dificuldade com Estrelas
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(point.difficulty) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = Amber500,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Nível ${point.difficulty}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Toque para abrir",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.error?.let {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Ops!") },
                    text = { Text(it) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.loadCityData() }) {
                            Text("Tentar Novamente")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onNavigateBack) {
                            Text("Voltar")
                        }
                    }
                )
            }
        }
    }
}