package com.questua.app.presentation.exploration.worldmap

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton

// ADICIONE ESTA ANOTAÇÃO AQUI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToCity: (String) -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Câmera inicial (pode ser ajustada para focar na primeira cidade)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    // Configuração do Estilo JSON
    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = try {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            } catch (e: Exception) { null },
            isMyLocationEnabled = false,
            maxZoomPreference = 12f,
            minZoomPreference = 2f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    }

    var selectedCityUiModel by remember { mutableStateOf<CityUiModel?>(null) }

    // Foca na primeira cidade quando carregar
    LaunchedEffect(state.cities) {
        if (state.cities.isNotEmpty() && cameraPositionState.position.zoom < 3f) {
            val first = state.cities.first().city
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(first.lat, first.lon), 4f
            )
        }
    }

    Scaffold(
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = { Text("Mapa Mundo") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { selectedCityUiModel = null }
            ) {
                state.cities.forEach { cityUi ->
                    // Converte os XML Drawables para BitmapDescriptor
                    val icon = bitmapDescriptorFromVector(
                        context,
                        if (cityUi.isUnlocked) R.drawable.ic_city_unlocked else R.drawable.ic_city_locked
                    )

                    Marker(
                        state = MarkerState(position = LatLng(cityUi.city.lat, cityUi.city.lon)),
                        title = cityUi.city.name,
                        icon = icon,
                        onClick = {
                            selectedCityUiModel = cityUi
                            true
                        }
                    )
                }
            }

            if (state.isLoading) {
                LoadingSpinner()
            }

            // Card da Cidade (Tooltip)
            selectedCityUiModel?.let { cityUi ->
                CityInfoCard(
                    cityUiModel = cityUi,
                    onClose = { selectedCityUiModel = null },
                    onActionClick = {
                        if (cityUi.isUnlocked) {
                            onNavigateToCity(cityUi.city.id)
                        } else {
                            viewModel.unlockCity(cityUi.city.id)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

// Função auxiliar para converter Vector Drawable (XML) em Bitmap para o Mapa
fun bitmapDescriptorFromVector(context: android.content.Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

@Composable
fun CityInfoCard(
    cityUiModel: CityUiModel,
    onClose: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (cityUiModel.isUnlocked) Icons.Default.LocationCity else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (cityUiModel.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cityUiModel.city.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cityUiModel.city.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuestuaButton(
                text = if (cityUiModel.isUnlocked) "Explorar Cidade" else "Desbloquear",
                onClick = onActionClick,
                isSecondary = !cityUiModel.isUnlocked
            )
        }
    }
}