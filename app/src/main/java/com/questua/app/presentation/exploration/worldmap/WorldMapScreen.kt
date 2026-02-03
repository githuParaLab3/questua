package com.questua.app.presentation.exploration.worldmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.questua.app.domain.model.City
import kotlinx.coroutines.launch

// Cor Dourada Padrão Questua
val QuestuaGold = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToCity: (String) -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado para controlar qual cidade está selecionada no mapa
    var selectedCity by remember { mutableStateOf<City?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 1f)
    }
    val scaffoldState = rememberBottomSheetScaffoldState()

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
                        selectedCity = cityUi.city // Seleciona a cidade
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(cityUi.city.lat, cityUi.city.lon),
                                10f
                            ),
                            1000
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
                    uiSettings = mapUiSettings,
                    onMapClick = {
                        selectedCity = null // Clicar no vazio fecha a seleção
                    }
                ) {
                    state.cities.forEach { cityUi ->
                        val city = cityUi.city

                        // CORREÇÃO CRÍTICA: Apenas UM marcador por cidade
                        MarkerComposable(
                            keys = arrayOf(city.id),
                            state = MarkerState(position = LatLng(city.lat, city.lon)),
                            onClick = {
                                scope.launch {
                                    selectedCity = city // Ativa o Card Flutuante
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lon), 8f)
                                    )
                                    // Fecha o sheet levemente para dar espaço ao card, se quiser
                                    // scaffoldState.bottomSheetState.partialExpand()
                                }
                                true // Consome o evento, não faz mais nada no mapa
                            }
                        ) {
                            QuestuaPinMarker(isSelected = selectedCity?.id == city.id)
                        }
                    }
                }

                // OVERLAY: Card Flutuante de Seleção (Substitui o InfoWindow nativo instável)
                // Aparece no topo da tela quando uma cidade é selecionada
                AnimatedVisibility(
                    visible = selectedCity != null,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp) // Espaço para não cobrir a toolbar se houver
                ) {
                    selectedCity?.let { city ->
                        CitySelectionOverlay(
                            city = city,
                            onAccess = { onNavigateToCity(city.id) },
                            onClose = { selectedCity = null }
                        )
                    }
                }

                // Botão Voltar
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

// --- Componentes ---

@Composable
fun CitySelectionOverlay(
    city: City,
    onAccess: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Text(
                text = city.countryCode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAccess,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestuaGold,
                    contentColor = Color.Black
                )
            ) {
                Text("ACESSAR CIDADE", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.PlayArrow, null)
            }
        }
    }
}

@Composable
fun WorldHubContent(
    cities: List<CityUiModel>,
    onCityClick: (CityUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )

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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cities, key = { it.city.id }) { cityUi ->
                CityHubCard(cityUi = cityUi, onClick = { onCityClick(cityUi) })
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
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                if (!city.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(city.imageUrl.toFullImageUrl())
                            .crossfade(true)
                            .size(300, 400)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
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
                    modifier = Modifier.fillMaxWidth().height(36.dp),
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

@Composable
fun QuestuaPinMarker(isSelected: Boolean = false) {
    val scale = if (isSelected) 1.2f else 1.0f
    val color = if (isSelected) Color.White else QuestuaGold

    Box(
        modifier = Modifier
            .size(48.dp * scale),
        contentAlignment = Alignment.BottomCenter
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = QuestuaGold,
            modifier = Modifier.size(48.dp * scale).shadow(8.dp, shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .padding(bottom = 18.dp * scale)
                .size(12.dp * scale)
                .clip(CircleShape)
                .background(color)
        )
    }
}

val TriangleShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
}