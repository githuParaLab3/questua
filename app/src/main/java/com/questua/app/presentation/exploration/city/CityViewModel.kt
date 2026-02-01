package com.questua.app.presentation.exploration.city

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.usecase.exploration.GetCityDetailsUseCase
import com.questua.app.domain.usecase.exploration.GetCityQuestPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val questPoints: List<QuestPoint> = emptyList(),
    val suggestedPoint: QuestPoint? = null,
    val hasActiveProgress: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CityViewModel @Inject constructor(
    private val getCityDetailsUseCase: GetCityDetailsUseCase,
    private val getCityQuestPointsUseCase: GetCityQuestPointsUseCase,
    // private val gameRepository: GameRepository, // Futuro: Usar para checar progresso real
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CityState())
    val state = _state.asStateFlow()

    private val cityId: String? = savedStateHandle["cityId"]

    init {
        loadCityData()
    }

    fun loadCityData() {
        if (cityId == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Combina as duas chamadas para atualizar o estado apenas quando ambas terminarem (ou fluírem)
            combine(
                getCityDetailsUseCase(cityId),
                getCityQuestPointsUseCase(cityId)
            ) { cityResult, pointsResult ->

                var newState = _state.value

                // Processa Cidade
                when (cityResult) {
                    is Resource.Success -> newState = newState.copy(city = cityResult.data)
                    is Resource.Error -> newState = newState.copy(error = cityResult.message)
                    is Resource.Loading -> Unit
                }

                // Processa Pontos
                when (pointsResult) {
                    is Resource.Success -> {
                        val points = pointsResult.data ?: emptyList()
                        newState = newState.copy(questPoints = points)

                        // Lógica de "Sugestão de Ponto"
                        val (suggested, hasProgress) = determineSuggestedPoint(points)
                        newState = newState.copy(
                            suggestedPoint = suggested,
                            hasActiveProgress = hasProgress
                        )
                    }
                    is Resource.Error -> newState = newState.copy(error = pointsResult.message)
                    is Resource.Loading -> Unit
                }

                // Define loading final
                val stillLoading = cityResult is Resource.Loading || pointsResult is Resource.Loading
                newState.copy(isLoading = stillLoading)

            }.collect { updatedState ->
                _state.value = updatedState
            }
        }
    }

    private fun determineSuggestedPoint(points: List<QuestPoint>): Pair<QuestPoint?, Boolean> {
        if (points.isEmpty()) return Pair(null, false)

        // Ordena por dificuldade (assumindo que dificuldade menor = início)
        val sortedPoints = points.sortedBy { it.difficulty }

        // TODO: Implementar verificação real de progresso do usuário
        // Aqui checaríamos no GameRepository se o usuário tem alguma quest "IN_PROGRESS"
        // em algum desses pontos.

        // Exemplo de lógica futura:
        /*
        val activePoint = sortedPoints.find { point ->
            gameRepository.hasUserStartedPoint(point.id)
        }
        if (activePoint != null) {
            return Pair(activePoint, true)
        }
        */

        // Padrão: Retorna o primeiro ponto (mais fácil) e flag false (significa "Começar")
        return Pair(sortedPoints.firstOrNull(), false)
    }
}