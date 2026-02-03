package com.questua.app.presentation.exploration.worldmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.common.toFullImageUrl
import kotlinx.coroutines.launch

// Cor Dourada Padrão Questua
val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)? = null, // Correção: Parâmetro opcional para evitar erro na MainScreen
    onNavigateToCity: (String) -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados de Câmera e Lista
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 1f)
    }
    val listState = rememberLazyListState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Estilo do Mapa Otimizado
    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = try {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            } catch (e: Exception) { null },
            isMyLocationEnabled = false,
            minZoomPreference = 2f,
            maxZoomPreference = 12f
        )
    }

    // Configurações de UI do Mapa (Leves)
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            WorldHubContent(
                cities = state.cities,
                onCityClick = { cityUi ->
                    scope.launch {
                        // Animação suave da câmera
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(cityUi.city.lat, cityUi.city.lon),
                                10f
                            ),
                            1000
                        )
                        // Navegação para detalhes
                        onNavigateToCity(cityUi.city.id)
                    }
                },
                onCardFocus = { cityUi ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(cityUi.city.lat, cityUi.city.lon),
                                6f
                            )
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { _ ->
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
                    uiSettings = mapUiSettings
                ) {
                    state.cities.forEach { cityUi ->
                        val city = cityUi.city

                        // MARCADOR LEVE (Sem AsyncImage para evitar crash)
                        MarkerComposable(
                            keys = arrayOf(city.id), // Chave estável
                            state = MarkerState(position = LatLng(city.lat, city.lon)),
                            onClick = {
                                scope.launch {
                                    // Foca no mapa e expande o hub
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lon), 8f)
                                    )
                                    scaffoldState.bottomSheetState.expand()
                                }
                                false
                            }
                        ) {
                            QuestuaPinMarker()
                        }
                    }
                }

                // Botão Voltar (Apenas se a função for fornecida)
                if (onNavigateBack != null) {
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
}

// --- Componentes Otimizados ---

@Composable
fun WorldHubContent(
    cities: List<CityUiModel>, // Usa o modelo UI correto
    onCityClick: (CityUiModel) -> Unit,
    onCardFocus: (CityUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )

        // Cabeçalho do Hub
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Explorar Mundo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${cities.size} destinos disponíveis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chip de Idioma
                Surface(
                    color = QuestuaGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, QuestuaGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, null, tint = QuestuaGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Inglês", color = QuestuaGold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lista Horizontal Otimizada
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cities, key = { it.city.id }) { cityUi ->
                CityHubCard(
                    cityUi = cityUi,
                    onClick = { onCityClick(cityUi) }
                )
                // Efeito colateral simples para focar ao rolar (opcional, pode ser removido se pesar)
                // LaunchedEffect(Unit) { onCardFocus(cityUi) }
            }
        }
    }
}

@Composable
fun CityHubCard(
    cityUi: CityUiModel,
    onClick: () -> Unit
) {
    val city = cityUi.city
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp) // Card mais compacto e horizontal
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Imagem (Esquerda)
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
            ) {
                if (!city.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(city.imageUrl.toFullImageUrl())
                            .crossfade(true)
                            .size(300, 400) // Downsampling para performance
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray)
                    }
                }
            }

            // Info (Direita)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = city.countryCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuestuaGold,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("VIAJAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

// Marcador Super Leve (Apenas Vetor)
@Composable
fun QuestuaPinMarker() {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Ícone Vetorial Puro (Zero alocação de bitmap pesado)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = QuestuaGold,
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, shape = CircleShape) // Sombra leve
        )
        // Ponto branco no centro para destaque
        Box(
            modifier = Modifier
                .padding(bottom = 18.dp) // Ajuste fino para o centro do LocationOn
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}