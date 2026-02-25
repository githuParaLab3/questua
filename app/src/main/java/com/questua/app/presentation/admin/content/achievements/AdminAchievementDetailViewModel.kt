package com.questua.app.presentation.admin.content.achievements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.usecase.admin.selectors.GetCitiesSelectorUseCase
import com.questua.app.domain.usecase.admin.selectors.GetQuestPointsSelectorUseCase
import com.questua.app.domain.usecase.admin.selectors.GetQuestsSelectorUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminAchievementDetailState(
    val isLoading: Boolean = false,
    val achievement: Achievement? = null,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val cities: List<City> = emptyList(),
    val quests: List<Quest> = emptyList(),
    val questPoints: List<QuestPoint> = emptyList(),
    val languages: List<Language> = emptyList()
)

@HiltViewModel
class AdminAchievementDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    private val getCitiesSelectorUseCase: GetCitiesSelectorUseCase,
    private val getQuestsSelectorUseCase: GetQuestsSelectorUseCase,
    private val getQuestPointsSelectorUseCase: GetQuestPointsSelectorUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminAchievementDetailState())
        private set

    private val achievementId: String = checkNotNull(savedStateHandle["achievementId"])

    init {
        fetchDetails()
        fetchSelectors()
    }

    fun fetchDetails() {
        repository.getAchievements(null).onEach { result ->
            if (result is Resource.Success) {
                val found = result.data?.find { it.id == achievementId }
                state = state.copy(achievement = found, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchSelectors() {
        getCitiesSelectorUseCase().onEach { res ->
            if (res is Resource.Success) state = state.copy(cities = res.data ?: emptyList())
        }.launchIn(viewModelScope)

        getQuestsSelectorUseCase().onEach { res ->
            if (res is Resource.Success) state = state.copy(quests = res.data ?: emptyList())
        }.launchIn(viewModelScope)

        getQuestPointsSelectorUseCase().onEach { res ->
            if (res is Resource.Success) state = state.copy(questPoints = res.data ?: emptyList())
        }.launchIn(viewModelScope)

        getAvailableLanguagesUseCase().onEach { res ->
            if (res is Resource.Success) state = state.copy(languages = res.data ?: emptyList())
        }.launchIn(viewModelScope)
    }

    fun saveAchievement(
        key: String, name: String, desc: String, icon: Any?,
        rarity: RarityType, xp: Int, isHidden: Boolean, isGlobal: Boolean,
        category: String, conditionType: AchievementConditionType, targetId: String,
        requiredAmount: Int
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            var finalIconUrl: String? = (icon as? String)
            if (icon is File) {
                repository.uploadFile(icon, "icons").collect { if (it is Resource.Success) finalIconUrl = it.data }
            }

            repository.saveAchievement(
                id = achievementId,
                keyName = key,
                nameAchievement = name,
                descriptionAchievement = desc.ifBlank { "" },
                iconUrl = finalIconUrl,
                rarity = rarity,
                xpReward = xp,
                isHidden = isHidden,
                isGlobal = isGlobal,
                category = category.ifBlank { null },
                conditionType = conditionType,
                targetId = targetId.ifBlank { null },
                requiredAmount = requiredAmount,
                metadata = null
            ).collect { result ->
                if (result is Resource.Success) fetchDetails()
                else state = state.copy(error = result.message, isLoading = false)
            }
        }
    }

    fun deleteAchievement() {
        repository.deleteAchievement(achievementId).onEach { result ->
            if (result is Resource.Success) state = state.copy(isDeleted = true)
        }.launchIn(viewModelScope)
    }
}