package com.questua.app.presentation.exploration.worldmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.City
import com.questua.app.domain.usecase.exploration.GetWorldMapUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorldMapState(
    val isLoading: Boolean = false,
    val cities: List<City> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val getWorldMapUseCase: GetWorldMapUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(WorldMapState())
    val state = _state.asStateFlow()

    init {
        loadMapData()
    }

    fun loadMapData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenManager.userId.first()

            if (userId != null) {
                // 1. Descobre qual idioma o usuário está aprendendo (para carregar o mapa certo)
                getUserStatsUseCase(userId).collect { statsResult ->
                    when (statsResult) {
                        is Resource.Success -> {
                            val languageId = statsResult.data!!.languageId
                            // 2. Carrega as cidades desse idioma
                            fetchCities(languageId)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Erro ao carregar estatísticas: ${statsResult.message}"
                            )
                        }
                        is Resource.Loading -> { /* Mantém loading */ }
                    }
                }
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Sessão inválida")
            }
        }
    }

    private suspend fun fetchCities(languageId: String) {
        getWorldMapUseCase(languageId).collect { result ->
            when (result) {
                is Resource.Loading -> {
                    // Já estamos em loading
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cities = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Erro ao carregar mapa"
                    )
                }
            }
        }
    }
}