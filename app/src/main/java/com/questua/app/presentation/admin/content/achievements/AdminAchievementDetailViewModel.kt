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
import com.questua.app.domain.repository.AdminRepository
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
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminAchievementDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminAchievementDetailState())
        private set

    private val achievementId: String = checkNotNull(savedStateHandle["achievementId"])

    init {
        fetchDetails()
    }

    fun fetchDetails() {
        repository.getAchievements(null).onEach { result ->
            if (result is Resource.Success) {
                val found = result.data?.find { it.id == achievementId }
                state = state.copy(achievement = found, isLoading = false)
            }
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
                metadata = null // FORÇADO A NULL
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