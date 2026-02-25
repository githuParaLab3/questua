package com.questua.app.core.ui.managers

import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.usecase.user.GetAchievementDetailsUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementMonitor @Inject constructor(
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getAchievementDetailsUseCase: GetAchievementDetailsUseCase,
    private val tokenManager: TokenManager
) {
    private val _currentPopup = MutableStateFlow<Achievement?>(null)
    val currentPopup: StateFlow<Achievement?> = _currentPopup.asStateFlow()

    private val _unseenAchievementIds = MutableStateFlow<Set<String>>(emptySet())
    val unseenAchievementIds: StateFlow<Set<String>> = _unseenAchievementIds.asStateFlow()

    private var knownAchievementIds: MutableSet<String> = mutableSetOf()
    private val pendingQueue = ArrayDeque<Achievement>()

    private var isProcessingQueue = false
    private var isInitialized = false

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun initialize() {
        if (isInitialized) return
        scope.launch {
            val userId = tokenManager.userId.first() ?: return@launch
            getUserAchievementsUseCase(userId).collect { result ->
                if (result is Resource.Success) {
                    val list = result.data ?: emptyList()
                    knownAchievementIds.clear()
                    knownAchievementIds.addAll(list.map { it.achievementId })
                    isInitialized = true
                }
            }
        }
    }

    fun check() {
        scope.launch {
            val userId = tokenManager.userId.first() ?: return@launch
            if (!isInitialized) {
                initialize()
                return@launch
            }

            getUserAchievementsUseCase(userId).collect { result ->
                if (result is Resource.Success) {
                    val currentList = result.data ?: emptyList()
                    val newUnlockUserAchievements = currentList.filter {
                        !knownAchievementIds.contains(it.achievementId)
                    }

                    if (newUnlockUserAchievements.isNotEmpty()) {
                        knownAchievementIds.addAll(newUnlockUserAchievements.map { it.achievementId })

                        val newIds = newUnlockUserAchievements.map { it.achievementId }.toSet()
                        _unseenAchievementIds.update { it + newIds }

                        newUnlockUserAchievements.forEach { userAchievement ->
                            fetchAndQueueDetails(userAchievement.achievementId)
                        }
                    }
                }
            }
        }
    }

    private fun fetchAndQueueDetails(achievementId: String) {
        scope.launch {
            getAchievementDetailsUseCase(achievementId).collect { result ->
                if (result is Resource.Success) {
                    result.data?.let { achievement ->
                        addToQueue(achievement)
                    }
                }
            }
        }
    }

    fun markAsSeen(achievementId: String) {
        _unseenAchievementIds.update { it - achievementId }
    }

    fun markAllAsSeen() {
        _unseenAchievementIds.update { emptySet() }
    }

    private fun addToQueue(achievement: Achievement) {
        pendingQueue.add(achievement)
        processQueue()
    }

    private fun processQueue() {
        if (isProcessingQueue || pendingQueue.isEmpty()) return
        isProcessingQueue = true
        val nextAchievement = pendingQueue.removeFirst()
        _currentPopup.update { nextAchievement }
        scope.launch {
            delay(4000)
            _currentPopup.update { null }
            delay(500)
            isProcessingQueue = false
            processQueue()
        }
    }
}