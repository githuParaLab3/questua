package com.questua.app.presentation.exploration.worldmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.questua.app.core.ui.components.LoadingSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)? = null, // Agora opcional (pode ser nulo)
    onNavigateToCity: (String) -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Posição inicial (ex: Europa)
    val defaultLocation = LatLng(48.8566, 2.3522)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 4f)
    }

    // Move a câmera para a primeira cidade carregada
    LaunchedEffect(state.cities) {
        if (state.cities.isNotEmpty()) {
            val firstCity = state.cities.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(firstCity.lat, firstCity.lon),
                5f
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mapa de Exploração",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    // Só mostra o botão se a função de voltar existir
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Fundo adaptável ao tema com transparência
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
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
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isMyLocationEnabled = false
                )
            ) {
                state.cities.forEach { city ->
                    Marker(
                        state = MarkerState(position = LatLng(city.lat, city.lon)),
                        title = city.name,
                        snippet = city.description,
                        onInfoWindowClick = {
                            onNavigateToCity(city.id)
                        }
                    )
                }
            }

            if (state.isLoading) {
                LoadingSpinner()
            }

            state.error?.let { errorMsg ->
                AlertDialog(
                    onDismissRequest = { /* Opcional: limpar erro */ },
                    confirmButton = {
                        TextButton(onClick = { viewModel.loadMapData() }) {
                            Text("Tentar Novamente")
                        }
                    },
                    dismissButton = {
                        if (onNavigateBack != null) {
                            TextButton(onClick = onNavigateBack) {
                                Text("Voltar")
                            }
                        }
                    },
                    title = { Text("Erro") },
                    text = { Text(errorMsg) }
                )
            }
        }
    }
}