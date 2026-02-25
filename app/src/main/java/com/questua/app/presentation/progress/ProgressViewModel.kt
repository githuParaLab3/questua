package com.questua.app.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserAchievement
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.onboarding.GetLanguageDetailsUseCase
import com.questua.app.domain.usecase.user.GetAchievementDetailsUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ProgressFilter {
    GLOBAL, ACTIVE_LANGUAGE
}

data class ProgressAchievementUiModel(
    val userAchievement: UserAchievement,
    val name: String,
    val description: String,
    val iconUrl: String?
)

data class ProgressState(
    val isLoading: Boolean = false,
    val filter: ProgressFilter = ProgressFilter.GLOBAL,
    val userLanguage: UserLanguage? = null,
    val globalXp: Int = 0,
    val globalQuestsCount: Int = 0,
    val globalQuestPointsCount: Int = 0,
    val globalCitiesCount: Int = 0,
    val activeQuestsCount: Int = 0,
    val activeQuestPointsCount: Int = 0,
    val activeCitiesCount: Int = 0,
    val globalLevel: Int = 0,
    val globalStreak: Int = 0,
    val bestStreakLanguageName: String? = null,
    val achievementsThisWeek: Int = 0,
    val achievementsThisMonth: Int = 0,
    val achievements: List<ProgressAchievementUiModel> = emptyList(),
    val languageDetails: Language? = null,
    val userId: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getAchievementDetailsUseCase: GetAchievementDetailsUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _filter = MutableStateFlow(ProgressFilter.GLOBAL)
    private val _state = MutableStateFlow(ProgressState())
    val state = _state.asStateFlow()

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    loadProgressData(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Sessão inválida")
                }
            }
        }
    }

    fun setFilter(newFilter: ProgressFilter) {
        _filter.value = newFilter
    }

    fun loadProgressData(userId: String) {
        _state.value = _state.value.copy(isLoading = true, userId = userId)

        viewModelScope.launch {
            combine(
                getUserStatsUseCase(userId),
                getUserLanguagesUseCase(userId),
                getUserAchievementsUseCase(userId),
                _filter
            ) { statsRes, allLangsRes, achievementsRes, currentFilter ->

                val activeUserLang = statsRes.data
                val allLangs = allLangsRes.data ?: emptyList()
                val rawAchievements = achievementsRes.data ?: emptyList()
                val error = statsRes.message ?: allLangsRes.message ?: achievementsRes.message

                val totalXp = allLangs.sumOf { it.xpTotal }
                val totalQuests = allLangs.sumOf { it.unlockedContent?.quests?.size ?: 0 }
                val totalQuestPoints = allLangs.sumOf { it.unlockedContent?.questPoints?.size ?: 0 }
                val totalCities = allLangs.sumOf { it.unlockedContent?.cities?.size ?: 0 }
                val totalLevel = allLangs.sumOf { it.gamificationLevel }

                val bestStreakUserLang = allLangs.maxByOrNull { it.streakDays }
                val bestStreakVal = bestStreakUserLang?.streakDays ?: 0
                val bestStreakLangName = if (bestStreakUserLang != null) {
                    val langRes = getLanguageDetailsUseCase(bestStreakUserLang.languageId)
                        .filter { it !is Resource.Loading }
                        .first()
                    langRes.data?.name
                } else null

                val currentQuests = activeUserLang?.unlockedContent?.quests?.size ?: 0
                val currentQuestPoints = activeUserLang?.unlockedContent?.questPoints?.size ?: 0
                val currentCities = activeUserLang?.unlockedContent?.cities?.size ?: 0

                val filteredAchievements = when (currentFilter) {
                    ProgressFilter.GLOBAL -> rawAchievements
                    ProgressFilter.ACTIVE_LANGUAGE -> {
                        val activeLangId = activeUserLang?.languageId
                        rawAchievements.filter { it.languageId == activeLangId }
                    }
                }.sortedByDescending { it.awardedAt }

                val now = try { LocalDateTime.now() } catch (e: Exception) { null }
                var countWeek = 0
                var countMonth = 0

                if (now != null) {
                    val oneWeekAgo = now.minusDays(7)
                    val oneMonthAgo = now.minusDays(30)
                    val formatters = listOf(
                        DateTimeFormatter.ISO_DATE_TIME,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )

                    filteredAchievements.forEach { ach ->
                        var date: LocalDateTime? = null
                        for (formatter in formatters) {
                            try {
                                date = LocalDateTime.parse(ach.awardedAt.replace("Z", ""), formatter)
                                break
                            } catch (e: Exception) { continue }
                        }
                        if (date != null) {
                            if (date.isAfter(oneWeekAgo)) countWeek++
                            if (date.isAfter(oneMonthAgo)) countMonth++
                        }
                    }
                }

                val uiAchievements = coroutineScope {
                    filteredAchievements.map { userAch ->
                        async {
                            val achDetailsResult = getAchievementDetailsUseCase(userAch.achievementId)
                                .filter { it !is Resource.Loading }
                                .first()
                            val details = achDetailsResult.data
                            ProgressAchievementUiModel(
                                userAchievement = userAch,
                                name = details?.name ?: "Conquista Secreta",
                                description = details?.description ?: "",
                                iconUrl = details?.iconUrl
                            )
                        }
                    }.awaitAll()
                }

                if (activeUserLang != null) {
                    fetchLanguageDetails(activeUserLang.languageId)
                }

                ProgressState(
                    filter = currentFilter,
                    userLanguage = activeUserLang,
                    globalXp = totalXp,
                    globalQuestsCount = totalQuests,
                    globalQuestPointsCount = totalQuestPoints,
                    globalCitiesCount = totalCities,
                    activeQuestsCount = currentQuests,
                    activeQuestPointsCount = currentQuestPoints,
                    activeCitiesCount = currentCities,
                    globalLevel = totalLevel,
                    globalStreak = bestStreakVal,
                    bestStreakLanguageName = bestStreakLangName,
                    achievements = uiAchievements,
                    achievementsThisWeek = countWeek,
                    achievementsThisMonth = countMonth,
                    userId = userId,
                    error = error
                )
            }.collect { result ->
                _state.value = result.copy(isLoading = false)
            }
        }
    }

    private fun fetchLanguageDetails(languageId: String) {
        viewModelScope.launch {
            getLanguageDetailsUseCase(languageId).collect { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(languageDetails = result.data)
                }
            }
        }
    }
}