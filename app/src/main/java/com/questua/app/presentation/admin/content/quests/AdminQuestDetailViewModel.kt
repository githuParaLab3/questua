package com.questua.app.presentation.admin.content.quests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminQuestDetailState(
    val isLoading: Boolean = false,
    val quest: Quest? = null,
    val questPoints: List<QuestPoint> = emptyList(),
    val dialogues: List<SceneDialogue> = emptyList(), // Dependência para o seletor
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
        fetchDetails()
        fetchDependencies()
    }

    fun fetchDetails() {
        // Idealmente teríamos um getQuestById, mas usando getQuests por enquanto como no AdminQuestViewModel
        repository.getQuests(null).onEach { result ->
            if (result is Resource.Success) {
                val found = result.data?.find { it.id == questId }
                state = state.copy(quest = found, isLoading = false)
            } else if (result is Resource.Loading) {
                state = state.copy(isLoading = true)
            } else if (result is Resource.Error) {
                state = state.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchDependencies() {
        repository.getQuestPoints(null).onEach {
            if (it is Resource.Success) state = state.copy(questPoints = it.data ?: emptyList())
        }.launchIn(viewModelScope)

        repository.getDialogues(null).onEach {
            if (it is Resource.Success) state = state.copy(dialogues = it.data ?: emptyList())
        }.launchIn(viewModelScope)
    }

    fun saveQuest(
        qpId: String, dialId: String?, title: String, desc: String,
        diff: Int, order: Int, xpValue: Int, xpPerQuestion: Int,
        unlock: UnlockRequirement?, focus: LearningFocus?,
        isPrem: Boolean, isAi: Boolean, isPub: Boolean
    ) {
        repository.saveQuest(
            questId, qpId, dialId, title, desc, diff, order, xpValue, xpPerQuestion, unlock, focus, isPrem, isAi, isPub
        ).onEach { result ->
            if (result is Resource.Success) {
                fetchDetails() // Recarrega os dados atualizados
            } else if (result is Resource.Error) {
                state = state.copy(error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteQuest() {
        repository.deleteQuest(questId).onEach { result ->
            if (result is Resource.Success) state = state.copy(isDeleted = true)
        }.launchIn(viewModelScope)
    }
}