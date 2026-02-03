package com.questua.app.presentation.exploration.city

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint

// Cor Dourada Padrão Questua
val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestPoint: (String) -> Unit,
    viewModel: CityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Configuração do Mapa
    val mapProperties = remember(state.city) {
        var bounds: LatLngBounds? = null

        state.city?.boundingPolygon?.coordinates?.let { coords ->
            if (coords.isNotEmpty()) {
                val builder = LatLngBounds.builder()
                coords.forEach { point ->
                    if (point.size >= 2) {
                        builder.include(LatLng(point[0], point[1]))
                    }
                }
                bounds = builder.build()
            }
        }

        MapProperties(
            mapStyleOptions = try {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            } catch (e: Exception) { null },
            latLngBoundsForCameraTarget = bounds,
            minZoomPreference = 13f,
            maxZoomPreference = 19f,
            isMyLocationEnabled = false
        )
    }

    LaunchedEffect(state.city) {
        state.city?.let { city ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(city.lat, city.lon),
                    15f
                )
            )
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 140.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            state.city?.let { city ->
                CityBottomSheetContent(city = city)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = QuestuaGold)
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false,
                        rotationGesturesEnabled = false
                    )
                ) {
                    // Contorno Dourado da Cidade
                    state.city?.boundingPolygon?.coordinates?.let { rawCoords ->
                        val polygonPoints = rawCoords.mapNotNull {
                            if (it.size >= 2) LatLng(it[0], it[1]) else null
                        }

                        if (polygonPoints.isNotEmpty()) {
                            Polygon(
                                points = polygonPoints,
                                strokeColor = QuestuaGold,
                                strokeWidth = 6f,
                                fillColor = QuestuaGold.copy(alpha = 0.15f)
                            )
                        }
                    }

                    // Renderização dos Pontos
                    state.questPoints.forEach { point ->

                        // MARCADOR NO MAPA (Ícone Personalizado)
                        MarkerComposable(
                            keys = arrayOf(point.id, point.iconUrl ?: "no_icon"),
                            state = MarkerState(position = LatLng(point.lat, point.lon)),
                            onClick = {
                                return@MarkerComposable false // Retorna false para abrir o InfoWindow padrão
                            }
                        ) {
                            QuestuaMapMarker(iconUrl = point.iconUrl)
                        }

                        // JANELA DE INFORMAÇÃO (Ao clicar no marcador)
                        MarkerInfoWindow(
                            state = MarkerState(position = LatLng(point.lat, point.lon)),
                            anchor = Offset(0.5f, 0.0f), // Ancora no topo do pino
                            onInfoWindowClick = {
                                // Ação de navegação segura
                                onNavigateToQuestPoint(point.id)
                            }
                        ) {
                            // Conteúdo visual da janela (Estático e leve para evitar crash)
                            SimpleInfoWindowContent(point = point)
                        }
                    }
                }

                // Botão Voltar
                SmallFloatingActionButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        }
    }
}

// --- Componentes Visuais ---

@Composable
fun QuestuaMapMarker(iconUrl: String?) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base do Pino (Triângulo)
        Box(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(16.dp)
                .background(QuestuaGold, shape = TriangleShape)
        )

        // Cabeça do Pino (Círculo)
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(QuestuaGold, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (!iconUrl.isNullOrBlank()) {
                // CORREÇÃO: Uso de toFullImageUrl()
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(iconUrl.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = QuestuaGold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SimpleInfoWindowContent(point: QuestPoint) {
    // InfoWindow simplificado para estabilidade (sem imagens pesadas, apenas texto e estilo)
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Indicador visual de ação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(
                    color = QuestuaGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = QuestuaGold,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ACESSAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = QuestuaGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CityBottomSheetContent(city: City) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = QuestuaGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.5f))
            ) {
                Text(
                    text = city.countryCode,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = QuestuaGold.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!city.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(city.imageUrl.toFullImageUrl()) // CORREÇÃO
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagem de ${city.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = city.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = 22.sp
        )
    }
}

val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
}