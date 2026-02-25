package com.questua.app.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.enums.ProgressStatus
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.model.UserQuest
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.ContentRepository
import com.questua.app.domain.repository.GameRepository
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

data class LevelProgress(
    val currentLevel: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val progress: Float,
    val levelsToNextCefr: Int,
    val nextCefrLabel: String
)

data class ResumeQuestItem(
    val userQuest: UserQuest,
    val questTitle: String,
    val questPointTitle: String
)

data class NewContentItem(
    val id: String,
    val title: String,
    val type: String, // "CITY" ou "QUEST"
    val imageUrl: String?,
    val isLocked: Boolean
)

data class HubState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val activeLanguage: UserLanguage? = null,
    val levelProgress: LevelProgress? = null,
    val continueJourneyQuests: List<ResumeQuestItem> = emptyList(),
    val latestAchievements: List<Achievement> = emptyList(),
    val newContent: List<NewContentItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase, // UseCase correto
    private val contentRepository: ContentRepository,
    private val gameRepository: GameRepository,
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager,
    private val achievementMonitor: AchievementMonitor
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
                }
            }
        }
    }

    private suspend fun <T> Flow<Resource<T>>.collectSuccess(): T? {
        return this.filter { it !is Resource.Loading }.firstOrNull()?.data
    }

    private fun loadHubData(userId: String) {
        _state.update { it.copy(isLoading = true) }

        getUserProfileUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.update { it.copy(user = result.data) }
                achievementMonitor.check()
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val userLanguage = getUserStatsUseCase(userId).collectSuccess()

            if (userLanguage != null) {
                val progress = calculateLevelProgress(userLanguage)

                val unlockedQuestsIds = userLanguage.unlockedContent?.quests ?: emptyList()
                val unlockedCitiesIds = userLanguage.unlockedContent?.cities ?: emptyList()

                // --- 1. Continue Journey ---
                // Pega as últimas quests desbloqueadas para checar se estão em progresso
                val candidates = unlockedQuestsIds.takeLast(20).reversed()

                val activeQuestsData = candidates.map { questId ->
                    async {
                        val status = gameRepository.getUserQuestStatus(questId, userId).collectSuccess()
                        if (status != null && status.status == ProgressStatus.IN_PROGRESS) {
                            val questDetails = gameRepository.getQuestById(questId).collectSuccess()
                            if (questDetails != null) {
                                // Busca o nome do QuestPoint (Local)
                                val questPoint = contentRepository.getQuestPointDetails(questDetails.questPointId).collectSuccess()
                                ResumeQuestItem(
                                    userQuest = status,
                                    questTitle = questDetails.title,
                                    questPointTitle = questPoint?.title ?: "Local"
                                )
                            } else null
                        } else null
                    }
                }.awaitAll().filterNotNull().take(5)

                // --- 2. Novidades (Cities + Quests do Idioma) ---
                val newContentItems = mutableListOf<NewContentItem>()
                val cities = contentRepository.getCities(userLanguage.languageId).collectSuccess() ?: emptyList()

                cities.takeLast(3).reversed().forEach { city ->
                    val isCityLocked = !unlockedCitiesIds.contains(city.id)

                    newContentItems.add(NewContentItem(
                        id = city.id,
                        title = city.name,
                        type = "CITY",
                        imageUrl = city.imageUrl,
                        isLocked = isCityLocked
                    ))

                    val points = contentRepository.getQuestPoints(city.id).collectSuccess() ?: emptyList()
                    points.forEach { point ->
                        val quests = contentRepository.getQuests(point.id).collectSuccess() ?: emptyList()
                        quests.takeLast(2).reversed().forEach { quest ->
                            if (newContentItems.size < 6) {
                                val isQuestLocked = !unlockedQuestsIds.contains(quest.id)
                                newContentItems.add(NewContentItem(
                                    id = quest.id,
                                    title = quest.title,
                                    type = "QUEST",
                                    imageUrl = null,
                                    isLocked = isQuestLocked
                                ))
                            }
                        }
                    }
                }

                // --- 3. Conquistas (Filtradas: Idioma/Global e NÃO possuídas) ---
                val allAchievements = adminRepository.getAchievements(null).collectSuccess() ?: emptyList()
                // Busca conquistas que o usuário já tem
                val userAchievements = getUserAchievementsUseCase(userId).collectSuccess() ?: emptyList()
                val userAchievementIds = userAchievements.map { it.achievementId }.toSet()

                val relevantAchievements = allAchievements
                    .filter {
                        (it.isGlobal || it.languageId == userLanguage.languageId) &&
                                !userAchievementIds.contains(it.id) // Exclui as que já tem
                    }
                    .takeLast(5)
                    .reversed()

                _state.update {
                    it.copy(
                        isLoading = false,
                        activeLanguage = userLanguage,
                        levelProgress = progress,
                        continueJourneyQuests = activeQuestsData,
                        latestAchievements = relevantAchievements,
                        newContent = newContentItems
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateLevelProgress(userLang: UserLanguage): LevelProgress {
        val currentLevel = userLang.gamificationLevel
        val currentXp = userLang.xpTotal
        val xpPerLevel = 1000
        val prevLevelXpThreshold = (currentLevel - 1) * xpPerLevel
        val xpInCurrentLevel = max(0, currentXp - prevLevelXpThreshold)
        val xpNeededForNext = xpPerLevel
        val progress = (xpInCurrentLevel.toFloat() / xpNeededForNext.toFloat()).coerceIn(0f, 1f)

        val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val currentCefrIndex = cefrLevels.indexOf(userLang.cefrLevel).takeIf { it >= 0 } ?: 0
        val nextCefrIndex = (currentCefrIndex + 1).coerceAtMost(cefrLevels.lastIndex)
        val nextCefrLabel = cefrLevels[nextCefrIndex]
        val levelsToNextCefr = 10

        return LevelProgress(
            currentLevel = currentLevel,
            currentXp = xpInCurrentLevel,
            nextLevelXp = xpNeededForNext,
            progress = progress,
            levelsToNextCefr = levelsToNextCefr,
            nextCefrLabel = nextCefrLabel
        )
    }
}