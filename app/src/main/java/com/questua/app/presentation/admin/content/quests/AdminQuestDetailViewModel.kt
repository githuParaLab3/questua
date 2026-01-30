package com.questua.app.presentation.admin.content.quests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminQuestDetailState(
    val isLoading: Boolean = false,
    val quest: Quest? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminQuestDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminQuestDetailState())
        private set

    private val questId: String = checkNotNull(savedStateHandle["questId"])

    init {
        fetchQuestDetails()
    }

    fun fetchQuestDetails() {
        repository.getQuests(query = questId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val foundQuest = result.data?.find { it.id == questId }
                    state.copy(
                        quest = foundQuest,
                        isLoading = false,
                        error = if (foundQuest == null) "Quest não encontrada" else null
                    )
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveQuest(
        pointId: String,
        title: String,
        desc: String,
        diff: Int,
        ord: Int,
        xp: Int,
        prem: Boolean,
        pub: Boolean
    ) {
        repository.saveQuest(questId, pointId, title, desc, diff, ord, xp, prem, pub).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    fetchQuestDetails()
                    state.copy(isLoading = false)
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteQuest() {
        repository.deleteQuest(questId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}