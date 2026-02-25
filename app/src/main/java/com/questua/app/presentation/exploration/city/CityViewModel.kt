package com.questua.app.presentation.exploration.city

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.usecase.exploration.GetCityDetailsUseCase
import com.questua.app.domain.usecase.exploration.GetCityQuestPointsUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val questPoints: List<QuestPoint> = emptyList(),
    val cityAchievements: List<Achievement> = emptyList(),
    val suggestedPoint: QuestPoint? = null,
    val hasActiveProgress: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CityViewModel @Inject constructor(
    private val getCityDetailsUseCase: GetCityDetailsUseCase,
    private val getCityQuestPointsUseCase: GetCityQuestPointsUseCase,
    private val adminRepository: AdminRepository,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val tokenManager: TokenManager,
    private val achievementMonitor: AchievementMonitor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CityState())
    val state = _state.asStateFlow()

    private val cityId: String? = savedStateHandle["cityId"]
    private var cityJob: Job? = null

    init {
        loadCityData()
        achievementMonitor.check()
    }

    fun refreshData() {
        loadCityData()
    }

    fun loadCityData() {
        if (cityId == null) return

        cityJob?.cancel()
        cityJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val userId = try {
                tokenManager.userId.first { !it.isNullOrEmpty() }
            } catch (e: Exception) { null }

            combine(
                getCityDetailsUseCase(cityId),
                getCityQuestPointsUseCase(cityId)
            ) { cityResult, pointsResult ->

                var newState = _state.value

                when (cityResult) {
                    is Resource.Success -> newState = newState.copy(city = cityResult.data)
                    is Resource.Error -> newState = newState.copy(error = cityResult.message)
                    is Resource.Loading -> Unit
                }

                when (pointsResult) {
                    is Resource.Success -> {
                        val points = pointsResult.data ?: emptyList()
                        newState = newState.copy(questPoints = points)

                        val (suggested, hasProgress) = determineSuggestedPoint(points)
                        newState = newState.copy(
                            suggestedPoint = suggested,
                            hasActiveProgress = hasProgress
                        )
                    }
                    is Resource.Error -> newState = newState.copy(error = pointsResult.message)
                    is Resource.Loading -> Unit
                }

                if (newState.city != null && userId != null) {
                    val achievements = loadCityAchievements(newState.city!!.id, userId)
                    newState = newState.copy(cityAchievements = achievements)
                }

                val stillLoading = cityResult is Resource.Loading || pointsResult is Resource.Loading
                newState.copy(isLoading = stillLoading)

            }.collect { updatedState ->
                _state.value = updatedState
            }
        }
    }

    private suspend fun loadCityAchievements(cityId: String, userId: String): List<Achievement> {
        return try {
            val allAchievementsJob = viewModelScope.async {
                adminRepository.getAchievements(null).filter { it !is Resource.Loading }.first().data ?: emptyList()
            }
            val userAchievementsJob = viewModelScope.async {
                getUserAchievementsUseCase(userId).filter { it !is Resource.Loading }.first().data ?: emptyList()
            }

            val allAchievements = allAchievementsJob.await()
            val userAchievements = userAchievementsJob.await()
            val userAchievementIds = userAchievements.map { it.achievementId }.toSet()

            allAchievements.filter { achievement ->
                val isTargeted = achievement.targetId == cityId
                val isRelatedType = achievement.conditionType.name == "UNLOCK_QUEST_POINT_AMOUNT" ||
                        achievement.conditionType.name == "COMPLETE_ALL_CITY_QUESTS" ||
                        achievement.conditionType.name == "UNLOCK_CITY_AMOUNT"

                (isTargeted || isRelatedType) && !userAchievementIds.contains(achievement.id)
            }.take(3)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun determineSuggestedPoint(points: List<QuestPoint>): Pair<QuestPoint?, Boolean> {
        if (points.isEmpty()) return Pair(null, false)
        val sortedPoints = points.sortedBy { it.difficulty }
        return Pair(sortedPoints.firstOrNull(), false)
    }
}