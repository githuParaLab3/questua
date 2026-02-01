package com.questua.app.presentation.exploration.worldmap

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)?,
    onNavigateToCity: (String) -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = try {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            } catch (e: Exception) { null },
            maxZoomPreference = 12f,
            minZoomPreference = 2f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)
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
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { viewModel.clearSelection() }
            ) {
                state.cities.forEach { cityUi ->
                    MarkerInfoWindowContent(
                        state = rememberMarkerState(position = LatLng(cityUi.city.lat, cityUi.city.lon)),
                        icon = bitmapDescriptorFromVector(
                            context,
                            if (cityUi.isUnlocked) R.drawable.ic_city_unlocked else R.drawable.ic_city_locked
                        ),
                        onClick = {
                            viewModel.loadCityProgress(cityUi.city.id)
                            false
                        }
                    ) {
                        CustomCityInfoWindow(
                            cityUi = cityUi,
                            progress = state.selectedCityProgress,
                            onAction = {
                                if (cityUi.isUnlocked) onNavigateToCity(cityUi.city.id)
                                else viewModel.unlockCity(cityUi.city.id)
                            }
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingSpinner()
            }
        }
    }
}

@Composable
fun CustomCityInfoWindow(
    cityUi: CityUiModel,
    progress: CityProgress?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = cityUi.city.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (cityUi.isUnlocked) {
                if (progress != null) {
                    Text(
                        text = "Progresso: ${progress.completedQuests}/${progress.totalQuests} missões",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.percentage },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val buttonText = if (progress.completedQuests == 0) "Explorar pontos culturais" else "Continuar jornada"

                    Button(
                        onClick = onAction,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(buttonText, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Text("Carregando progresso...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text(
                    text = cityUi.city.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Desbloquear")
                }
            }
        }
    }
}

fun bitmapDescriptorFromVector(context: android.content.Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}