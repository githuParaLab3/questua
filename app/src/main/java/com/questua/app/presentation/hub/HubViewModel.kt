package com.questua.app.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.repository.UserRepository
import com.questua.app.domain.usecase.onboarding.GetLanguageDetailsUseCase
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubCityItem(val city: City, val isLocked: Boolean)
data class HubQuestItem(val quest: Quest, val isLocked: Boolean)
data class HubPointItem(val point: QuestPoint, val isLocked: Boolean)

data class HubState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val activeLanguage: UserLanguage? = null,
    val currentLanguageDetails: Language? = null,
    val continueJourneyQuests: List<UserQuest> = emptyList(),
    val latestCities: List<HubCityItem> = emptyList(),
    val latestQuests: List<HubQuestItem> = emptyList(),
    val latestQuestPoints: List<HubPointItem> = emptyList(),
    val latestSystemAchievements: List<Achievement> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val gameRepository: GameRepository,
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(HubState())
    val state = _state.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeUserSession()
    }

    fun refreshData() {
        currentUserId?.let { loadHubData(it) }
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    currentUserId = userId
                    loadHubData(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadHubData(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        getUserProfileUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(user = result.data)
            }
        }.launchIn(viewModelScope)

        getUserStatsUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                val userLang = result.data
                _state.value = _state.value.copy(activeLanguage = userLang)

                userLang?.let { lang ->
                    fetchLanguageDetails(lang.languageId)
                    loadContinueJourney(userId, lang)
                    loadNewContent(lang)
                }
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)

        loadSystemAchievements()
    }

    private fun loadContinueJourney(userId: String, userLanguage: UserLanguage) {
        viewModelScope.launch {
            val unlockedIds = userLanguage.unlockedContent?.quests ?: emptyList()
            if (unlockedIds.isEmpty()) {
                _state.value = _state.value.copy(continueJourneyQuests = emptyList())
                return@launch
            }

            // Otimização: Pegar apenas os últimos 10 desbloqueados para checar status
            val recentUnlocked = unlockedIds.takeLast(10)

            val deferreds = recentUnlocked.map { questId ->
                async { gameRepository.getUserQuestStatus(questId, userId).firstOrNull() }
            }

            val results = deferreds.awaitAll()

            val inProgress = results.mapNotNull { it?.data }
                .filter { it.status == ProgressStatus.IN_PROGRESS }
                // Ordena por lastActivityAt (supondo que UserQuest tenha esse campo corrigido)
                // Se UserQuest não tiver data, o reversed() da lista de desbloqueio serve como fallback cronológico
                .sortedByDescending { it.lastActivityAt }
                .take(3)

            _state.value = _state.value.copy(continueJourneyQuests = inProgress)
        }
    }

    private fun loadNewContent(userLanguage: UserLanguage) {
        viewModelScope.launch {
            val unlocked = userLanguage.unlockedContent

            contentRepository.getCities(userLanguage.languageId).collect { result ->
                if (result is Resource.Success) {
                    val items = result.data?.takeLast(3)?.reversed()?.map { city ->
                        HubCityItem(city, unlocked?.cities?.contains(city.id) != true)
                    } ?: emptyList()
                    _state.value = _state.value.copy(latestCities = items)
                }
            }

            adminRepository.getAllQuests(page = 0, size = 5).collect { result ->
                if (result is Resource.Success) {
                    val items = result.data?.take(3)?.map { quest ->
                        HubQuestItem(quest, unlocked?.quests?.contains(quest.id) != true)
                    } ?: emptyList()
                    _state.value = _state.value.copy(latestQuests = items)
                }
            }

            adminRepository.getAllQuestPoints(page = 0, size = 5).collect { result ->
                if (result is Resource.Success) {
                    val items = result.data?.take(3)?.map { point ->
                        HubPointItem(point, unlocked?.questPoints?.contains(point.id) != true)
                    } ?: emptyList()
                    _state.value = _state.value.copy(latestQuestPoints = items)
                }
            }

            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun loadSystemAchievements() {
        viewModelScope.launch {
            adminRepository.getAchievements(query = null).collect { result ->
                if (result is Resource.Success) {
                    val recent = result.data?.takeLast(3)?.reversed() ?: emptyList()
                    _state.value = _state.value.copy(latestSystemAchievements = recent)
                }
            }
        }
    }

    private fun fetchLanguageDetails(languageId: String) {
        getLanguageDetailsUseCase(languageId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(currentLanguageDetails = result.data)
            }
        }.launchIn(viewModelScope)
    }
}