package com.questua.app.presentation.admin.content.quests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Quest
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
        fetchQuests()
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Aguarda 500ms após o usuário parar de digitar
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

    fun saveQuest(id: String?, pointId: String, title: String, desc: String, diff: Int, ord: Int, xp: Int, prem: Boolean, pub: Boolean) {
        repository.saveQuest(id, pointId, title, desc, diff, ord, xp, prem, pub).onEach { result ->
            if (result is Resource.Success) fetchQuests()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }

    fun deleteQuest(id: String) {
        repository.deleteQuest(id).onEach { result ->
            if (result is Resource.Success) fetchQuests()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}