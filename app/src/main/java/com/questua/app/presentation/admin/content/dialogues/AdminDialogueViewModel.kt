package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.*
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
    val characters: List<CharacterEntity> = emptyList(),
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

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchDialogues()
        fetchCharacters()
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchDialogues()
        }
    }

    private fun fetchDialogues() {
        repository.getDialogues(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.dialogues.isEmpty())
                is Resource.Success -> state.copy(dialogues = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchCharacters() {
        repository.getCharacters(null).onEach { result ->
            if (result is Resource.Success) state = state.copy(characters = result.data ?: emptyList())
        }.launchIn(viewModelScope)
    }

    fun saveDialogue(
        txt: String, desc: String, bg: String, music: String?,
        states: List<CharacterState>?, effects: List<SceneEffect>?,
        speaker: String?, audio: String?, expects: Boolean,
        mode: InputMode, expectResp: String?, choices: List<Choice>?,
        next: String?, ai: Boolean
    ) {
        repository.saveDialogue(
            null, txt, desc, bg, music, states, effects, speaker, audio, expects, mode, expectResp, choices, next, ai
        ).onEach { result ->
            if (result is Resource.Success) fetchDialogues()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}