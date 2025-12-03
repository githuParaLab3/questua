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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    // Novos campos de estatísticas
    val globalXp: Int = 0,
    val globalQuestsCount: Int = 0,
    val activeQuestsCount: Int = 0,

    val achievements: List<ProgressAchievementUiModel> = emptyList(),
    val languageDetails: Language? = null,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase, // Injetado para pegar dados globais
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getAchievementDetailsUseCase: GetAchievementDetailsUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

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
        _state.value = _state.value.copy(filter = newFilter)
    }

    fun loadProgressData(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            // Combinamos 3 fontes de dados: Estatísticas do idioma ativo, Todos os idiomas (global), e Conquistas
            combine(
                getUserStatsUseCase(userId),
                getUserLanguagesUseCase(userId),
                getUserAchievementsUseCase(userId)
            ) { statsRes, allLangsRes, achievementsRes ->

                val activeUserLang = statsRes.data
                val allLangs = allLangsRes.data ?: emptyList()
                val rawAchievements = achievementsRes.data ?: emptyList()

                val error = statsRes.message ?: allLangsRes.message ?: achievementsRes.message

                // --- Lógica de Estatísticas Globais ---
                val totalXp = allLangs.sumOf { it.xpTotal }
                // Soma o tamanho da lista de IDs 'quests' em unlockedContent de cada idioma
                val totalQuests = allLangs.sumOf { it.unlockedContent?.quests?.size ?: 0 }

                // --- Lógica do Idioma Ativo ---
                val currentQuests = activeUserLang?.unlockedContent?.quests?.size ?: 0

                // Carrega nomes das conquistas (assíncrono)
                val uiAchievements = rawAchievements.map { userAch ->
                    async {
                        val achDetailsResult = getAchievementDetailsUseCase(userAch.achievementId)
                            .filter { it !is Resource.Loading }
                            .first()

                        val name = achDetailsResult.data?.name ?: "Conquista Secreta"
                        ProgressAchievementUiModel(userAch, name)
                    }
                }.awaitAll()

                if (activeUserLang != null) {
                    fetchLanguageDetails(activeUserLang.languageId)
                }

                // Retorna um objeto intermediário com tudo calculado
                ProgressDataResult(
                    activeLang = activeUserLang,
                    globalXp = totalXp,
                    globalQuests = totalQuests,
                    activeQuests = currentQuests,
                    achievements = uiAchievements,
                    error = error
                )
            }.collect { result ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    userLanguage = result.activeLang,
                    globalXp = result.globalXp,
                    globalQuestsCount = result.globalQuests,
                    activeQuestsCount = result.activeQuests,
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

    // Data class auxiliar interna para passar os dados do combine
    private data class ProgressDataResult(
        val activeLang: UserLanguage?,
        val globalXp: Int,
        val globalQuests: Int,
        val activeQuests: Int,
        val achievements: List<ProgressAchievementUiModel>,
        val error: String?
    )
}