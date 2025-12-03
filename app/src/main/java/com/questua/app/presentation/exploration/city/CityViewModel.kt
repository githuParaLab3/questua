package com.questua.app.presentation.exploration.city

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.usecase.exploration.GetCityDetailsUseCase
import com.questua.app.domain.usecase.exploration.GetCityQuestPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityDetailState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val questPoints: List<QuestPoint> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CityViewModel @Inject constructor(
    private val getCityDetailsUseCase: GetCityDetailsUseCase,
    private val getCityQuestPointsUseCase: GetCityQuestPointsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CityDetailState())
    val state = _state.asStateFlow()

    // Pega o ID da cidade passado pela navegação
    private val cityId: String? = savedStateHandle["cityId"]

    init {
        loadCityData()
    }

    fun loadCityData() {
        if (cityId == null) {
            _state.value = _state.value.copy(error = "Cidade não identificada")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Carrega Detalhes e Pontos em paralelo
            getCityDetailsUseCase(cityId).zip(getCityQuestPointsUseCase(cityId)) { cityRes, pointsRes ->
                Pair(cityRes, pointsRes)
            }.collect { (cityRes, pointsRes) ->
                val isLoading = cityRes is Resource.Loading || pointsRes is Resource.Loading
                val error = cityRes.message ?: pointsRes.message

                if (!isLoading) {
                    if (cityRes is Resource.Success) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            city = cityRes.data,
                            questPoints = pointsRes.data ?: emptyList(),
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error ?: "Erro ao carregar cidade"
                        )
                    }
                }
            }
        }
    }
}