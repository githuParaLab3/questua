package com.questua.app.presentation.exploration.worldmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.City
import com.questua.app.domain.usecase.exploration.GetWorldMapUseCase
import com.questua.app.domain.usecase.exploration.UnlockContentUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityUiModel(
    val city: City,
    val isUnlocked: Boolean
)

data class WorldMapState(
    val isLoading: Boolean = false,
    val cities: List<CityUiModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val getWorldMapUseCase: GetWorldMapUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val unlockContentUseCase: UnlockContentUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(WorldMapState())
    val state = _state.asStateFlow()

    private var mapJob: Job? = null

    init {
        loadMapData()
    }

    fun refreshData() {
        loadMapData()
    }

    fun loadMapData() {
        mapJob?.cancel()
        mapJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenManager.userId.first()

            if (userId != null) {
                // Pega stats para saber o idioma e cidades desbloqueadas
                getUserStatsUseCase(userId).collect { statsResult ->
                    when (statsResult) {
                        is Resource.Success -> {
                            val userLang = statsResult.data!!
                            val langId = userLang.languageId
                            val unlockedIds = userLang.unlockedContent?.cities ?: emptyList()

                            fetchCities(langId, unlockedIds)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(isLoading = false, error = statsResult.message)
                        }
                        is Resource.Loading -> {}
                    }
                }
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Usuário não logado")
            }
        }
    }

    private suspend fun fetchCities(languageId: String, unlockedIds: List<String>) {
        getWorldMapUseCase(languageId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val cityList = result.data ?: emptyList()
                    val uiList = cityList.map { city ->
                        CityUiModel(city, isUnlocked = unlockedIds.contains(city.id))
                    }
                    _state.value = _state.value.copy(isLoading = false, cities = uiList)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun unlockCity(cityId: String) {
        // Implementar lógica real de desbloqueio aqui (chamar useCase)
        // Por enquanto, apenas um placeholder
    }
}