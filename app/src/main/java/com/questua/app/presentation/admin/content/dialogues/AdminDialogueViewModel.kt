package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDialogueState(
    val isLoading: Boolean = false,
    val dialogues: List<SceneDialogue> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminDialogueViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminDialogueState())
        private set

    private var searchJob: Job? = null

    init { fetchDialogues() }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchDialogues()
        }
    }

    fun fetchDialogues() {
        repository.getDialogues(state.searchQuery).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.dialogues.isEmpty())
                is Resource.Success -> state.copy(dialogues = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveDialogue(id: String?, text: String, desc: String, bg: String, speakerId: String?, expects: Boolean, mode: com.questua.app.domain.enums.InputMode, next: String?) {
        repository.saveDialogue(id, text, desc, bg, speakerId, expects, mode, next).onEach { result ->
            if (result is Resource.Success) fetchDialogues()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }

    fun deleteDialogue(id: String) {
        repository.deleteDialogue(id).onEach { result ->
            if (result is Resource.Success) fetchDialogues()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}