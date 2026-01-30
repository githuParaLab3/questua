package com.questua.app.presentation.admin.content.achievements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        repository.getAchievements(query = achievementId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val found = result.data?.find { it.id == achievementId }
                    state.copy(
                        achievement = found,
                        isLoading = false,
                        error = if (found == null) "Conquista não encontrada" else null
                    )
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveAchievement(
        name: String,
        description: String,
        iconUrl: String,
        xpReward: Int,
        keyName: String,
        rarity: RarityType
    ) {
        repository.saveAchievement(achievementId, name, description, iconUrl, xpReward, keyName, rarity).onEach { result ->
            if (result is Resource.Success) fetchDetails()
        }.launchIn(viewModelScope)
    }

    fun deleteAchievement() {
        repository.deleteAchievement(achievementId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}