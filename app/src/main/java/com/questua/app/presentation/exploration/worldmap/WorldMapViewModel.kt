package com.questua.app.presentation.exploration.worldmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.City
import com.questua.app.domain.usecase.exploration.GetWorldMapUseCase
import com.questua.app.domain.usecase.exploration.UnlockContentUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import com.questua.app.domain.usecase.exploration.GetCityQuestPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityUiModel(
    val city: City,
    val isUnlocked: Boolean
)

data class CityProgress(
    val totalQuests: Int = 0,
    val completedQuests: Int = 0,
    val percentage: Float = 0f
)

data class WorldMapState(
    val isLoading: Boolean = false,
    val cities: List<CityUiModel> = emptyList(),
    val selectedCityProgress: CityProgress? = null,
    val error: String? = null
)

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val getWorldMapUseCase: GetWorldMapUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val unlockContentUseCase: UnlockContentUseCase,
    private val getCityQuestPointsUseCase: GetCityQuestPointsUseCase,
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

    fun loadCityProgress(cityId: String) {
        viewModelScope.launch {
            val userId = tokenManager.userId.first() ?: return@launch

            getCityQuestPointsUseCase(cityId).collect { result ->
                if (result is Resource.Success) {
                    val points = result.data ?: emptyList()
                    // Lógica para cruzar pontos com missões concluídas no perfil do usuário
                    // Placeholder para demonstração de progresso
                    val total = points.size * 2 // Exemplo: 2 missões por ponto
                    val completed = 0 // Buscar do UserLanguage.unlockedContent.quests

                    _state.value = _state.value.copy(
                        selectedCityProgress = CityProgress(
                            totalQuests = total,
                            completedQuests = completed,
                            percentage = if (total > 0) completed.toFloat() / total else 0f
                        )
                    )
                }
            }
        }
    }

    fun unlockCity(cityId: String) {
        viewModelScope.launch {
            unlockContentUseCase(cityId, "city").collect { result ->
                if (result is Resource.Success) {
                    loadMapData()
                }
            }
        }
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedCityProgress = null)
    }
}