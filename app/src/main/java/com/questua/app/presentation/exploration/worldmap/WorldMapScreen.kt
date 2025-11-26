package com.questua.app.presentation.exploration.worldmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCity: (String) -> Unit, // Callback para abrir detalhes da cidade
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Posição inicial da câmera (Pode ser ajustada para focar na primeira cidade da lista futuramente)
    val defaultLocation = LatLng(48.8566, 2.3522) // Ex: Paris
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 4f)
    }

    // Efeito para mover a câmera se a lista de cidades carregar e não estiver vazia
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
        // TopBar flutuante/transparente para maximizar a área do mapa
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Exploração", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900.copy(alpha = 0.7f) // Fundo semitransparente
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
                    myLocationButtonEnabled = false // Habilitar se pedir permissão de localização
                ),
                properties = MapProperties(
                    isMyLocationEnabled = false // Requer permissão ACCESS_FINE_LOCATION
                )
            ) {
                // Renderiza os marcadores das cidades
                state.cities.forEach { city ->
                    Marker(
                        state = MarkerState(position = LatLng(city.lat, city.lon)),
                        title = city.name,
                        snippet = city.description, // Exibe descrição ao clicar
                        onInfoWindowClick = {
                            onNavigateToCity(city.id)
                        }
                    )
                }
            }

            // Loading Overlay
            if (state.isLoading) {
                LoadingSpinner()
            }

            // Error Handling
            state.error?.let {
                // Exibe erro mas permite tentar novamente recarregando a tela ou um botão manual
                // Aqui usamos um diálogo simples por enquanto
                AlertDialog(
                    onDismissRequest = { /* Opcional: limpar erro */ },
                    confirmButton = {
                        TextButton(onClick = { viewModel.loadMapData() }) {
                            Text("Tentar Novamente")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onNavigateBack) {
                            Text("Voltar")
                        }
                    },
                    title = { Text("Erro") },
                    text = { Text(it) }
                )
            }
        }
    }
}