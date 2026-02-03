package com.questua.app.presentation.exploration.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.domain.model.City

@Composable
fun CityDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestPoint: (String) -> Unit,
    viewModel: CityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

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
            minZoomPreference = 12f,
            maxZoomPreference = 18f,
            isMyLocationEnabled = false
        )
    }

    LaunchedEffect(state.city) {
        state.city?.let { city ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(city.lat, city.lon),
                    14f
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    )
                ) {
                    state.city?.boundingPolygon?.coordinates?.let { rawCoords ->
                        val polygonPoints = rawCoords.mapNotNull {
                            if (it.size >= 2) LatLng(it[0], it[1]) else null
                        }

                        if (polygonPoints.isNotEmpty()) {
                            Polygon(
                                points = polygonPoints,
                                strokeColor = Color(0xFFFFC107),
                                strokeWidth = 5f,
                                fillColor = Color(0x22FFC107)
                            )
                        }
                    }

                    state.questPoints.forEach { point ->
                        Marker(
                            state = MarkerState(position = LatLng(point.lat, point.lon)),
                            title = point.title,
                            snippet = point.description,
                            onInfoWindowClick = {
                                onNavigateToQuestPoint(point.id)
                            }
                        )
                    }
                }

                SmallFloatingActionButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }

                state.city?.let { city ->
                    CityInfoCard(
                        city = city,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
fun CityInfoCard(
    city: City,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!city.imageUrl.isNullOrBlank()) {
                QuestuaAsyncImage(
                    imageUrl = city.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = city.countryCode,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = city.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 4
                )
            }
        }
    }
}