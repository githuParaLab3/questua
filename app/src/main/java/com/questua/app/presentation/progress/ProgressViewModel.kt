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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProgressFilter {
    GLOBAL, ACTIVE_LANGUAGE
}

data class ProgressAchievementUiModel(
    val userAchievement: UserAchievement,
    val name: String
)

data class ProgressState(
    val isLoading: Boolean = false,
    val filter: ProgressFilter = ProgressFilter.ACTIVE_LANGUAGE,
    val userLanguage: UserLanguage? = null,

    // Estatísticas
    val globalXp: Int = 0,
    val globalQuestsCount: Int = 0,
    val globalQuestPointsCount: Int = 0, // Novo
    val globalCitiesCount: Int = 0,      // Novo
    val activeQuestsCount: Int = 0,
    val activeQuestPointsCount: Int = 0, // Novo
    val activeCitiesCount: Int = 0,      // Novo

    val globalLevel: Int = 0,
    val globalStreak: Int = 0,
    val bestStreakLanguageName: String? = null,

    val achievements: List<ProgressAchievementUiModel> = emptyList(),
    val languageDetails: Language? = null,
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

    private val _filter = MutableStateFlow(ProgressFilter.ACTIVE_LANGUAGE)

    private val _state = MutableStateFlow(ProgressState())
    val state = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
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
        _state.value = _state.value.copy(isLoading = true)

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

                // --- Lógica Global ---
                val totalXp = allLangs.sumOf { it.xpTotal }
                val totalQuests = allLangs.sumOf { it.unlockedContent?.quests?.size ?: 0 }
                val totalQuestPoints = allLangs.sumOf { it.unlockedContent?.questPoints?.size ?: 0 }
                val totalCities = allLangs.sumOf { it.unlockedContent?.cities?.size ?: 0 }

                val totalLevel = allLangs.sumOf { it.gamificationLevel }

                // Ofensiva Global
                val bestStreakUserLang = allLangs.maxByOrNull { it.streakDays }
                val bestStreakVal = bestStreakUserLang?.streakDays ?: 0

                val bestStreakLangName = if (bestStreakUserLang != null) {
                    val langRes = getLanguageDetailsUseCase(bestStreakUserLang.languageId)
                        .filter { it !is Resource.Loading }
                        .first()
                    langRes.data?.name
                } else null

                // --- Lógica Ativa ---
                val currentQuests = activeUserLang?.unlockedContent?.quests?.size ?: 0
                val currentQuestPoints = activeUserLang?.unlockedContent?.questPoints?.size ?: 0
                val currentCities = activeUserLang?.unlockedContent?.cities?.size ?: 0

                // --- Filtragem de Conquistas (CORRIGIDO) ---
                val filteredAchievements = when (currentFilter) {
                    ProgressFilter.GLOBAL -> rawAchievements // Mostra tudo (Globais + Idiomas)
                    ProgressFilter.ACTIVE_LANGUAGE -> {
                        val activeLangId = activeUserLang?.languageId
                        // Mostra APENAS conquistas deste idioma específico.
                        // Removemos a verificação "|| it.languageId == null" para não mostrar globais aqui.
                        rawAchievements.filter { it.languageId == activeLangId }
                    }
                }

                // Carrega nomes das conquistas
                val uiAchievements = coroutineScope {
                    filteredAchievements.map { userAch ->
                        async {
                            val achDetailsResult = getAchievementDetailsUseCase(userAch.achievementId)
                                .filter { it !is Resource.Loading }
                                .first()

                            val name = achDetailsResult.data?.name ?: "Conquista Secreta"
                            ProgressAchievementUiModel(userAch, name)
                        }
                    }.awaitAll()
                }

                if (activeUserLang != null) {
                    fetchLanguageDetails(activeUserLang.languageId)
                }

                ProgressDataResult(
                    filter = currentFilter,
                    activeLang = activeUserLang,
                    globalXp = totalXp,
                    globalQuests = totalQuests,
                    globalQuestPoints = totalQuestPoints,
                    globalCities = totalCities,
                    activeQuests = currentQuests,
                    activeQuestPoints = currentQuestPoints,
                    activeCities = currentCities,
                    globalLevel = totalLevel,
                    globalStreak = bestStreakVal,
                    bestStreakLangName = bestStreakLangName,
                    achievements = uiAchievements,
                    error = error
                )
            }.collect { result ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    filter = result.filter,
                    userLanguage = result.activeLang,
                    globalXp = result.globalXp,
                    globalQuestsCount = result.globalQuests,
                    globalQuestPointsCount = result.globalQuestPoints,
                    globalCitiesCount = result.globalCities,
                    activeQuestsCount = result.activeQuests,
                    activeQuestPointsCount = result.activeQuestPoints,
                    activeCitiesCount = result.activeCities,
                    globalLevel = result.globalLevel,
                    globalStreak = result.globalStreak,
                    bestStreakLanguageName = result.bestStreakLangName,
                    achievements = result.achievements,
                    error = result.error
                )
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

    private data class ProgressDataResult(
        val filter: ProgressFilter,
        val activeLang: UserLanguage?,
        val globalXp: Int,
        val globalQuests: Int,
        val globalQuestPoints: Int,
        val globalCities: Int,
        val activeQuests: Int,
        val activeQuestPoints: Int,
        val activeCities: Int,
        val globalLevel: Int,
        val globalStreak: Int,
        val bestStreakLangName: String?,
        val achievements: List<ProgressAchievementUiModel>,
        val error: String?
    )
}