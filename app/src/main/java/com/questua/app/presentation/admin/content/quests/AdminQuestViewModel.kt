package com.questua.app.presentation.admin.content.quests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminQuestState(
    val isLoading: Boolean = false,
    val quests: List<Quest> = emptyList(),
    val questPoints: List<QuestPoint> = emptyList(), // Dependência
    val dialogues: List<SceneDialogue> = emptyList(), // Dependência
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminQuestViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminQuestState())
        private set

    private var searchJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchQuests()
        fetchDependencies()
    }

    private fun fetchDependencies() {
        // Busca QuestPoints para o seletor
        repository.getQuestPoints(null).onEach { result ->
            if (result is Resource.Success) state = state.copy(questPoints = result.data ?: emptyList())
        }.launchIn(viewModelScope)

        // Busca Dialogues para o seletor (opcional, mas bom ter)
        repository.getDialogues(null).onEach { result ->
            if (result is Resource.Success) state = state.copy(dialogues = result.data ?: emptyList())
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchQuests()
        }
    }

    fun fetchQuests() {
        repository.getQuests(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.quests.isEmpty())
                is Resource.Success -> state.copy(quests = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveQuest(
        id: String?, qpId: String, dialId: String?, title: String, desc: String,
        diff: Int, order: Int, xpValue: Int, xpPerQuestion: Int,
        unlock: UnlockRequirement?, focus: LearningFocus?,
        isPrem: Boolean, isAi: Boolean, isPub: Boolean
    ) {
        repository.saveQuest(
            id, qpId, dialId, title, desc, diff, order, xpValue, xpPerQuestion, unlock, focus, isPrem, isAi, isPub
        ).onEach { result ->
            if (result is Resource.Success) fetchQuests()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}