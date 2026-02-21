package com.questua.app.presentation.admin.content.achievements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminAchievementState(
    val isLoading: Boolean = false,
    val achievements: List<Achievement> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminAchievementViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminAchievementState())
        private set

    private var searchJob: Job? = null

    init {
        fetchAchievements()
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchAchievements()
        }
    }

    fun fetchAchievements() {
        repository.getAchievements(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.achievements.isEmpty())
                is Resource.Success -> state.copy(achievements = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveAchievement(
        id: String?, key: String, name: String, desc: String, icon: Any?,
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
                id = id,
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
                if (result is Resource.Success) fetchAchievements()
                else if (result is Resource.Error) state = state.copy(error = result.message, isLoading = false)
            }
        }
    }
}