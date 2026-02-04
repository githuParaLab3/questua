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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
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

    // CORREÇÃO: Parâmetros bg, music e audio agora são Any?
    fun saveDialogue(
        txt: String, desc: String, bg: Any?, music: Any?,
        states: List<CharacterState>?, effects: List<SceneEffect>?,
        speaker: String?, audio: Any?, expects: Boolean,
        mode: InputMode, expectResp: String?, choices: List<Choice>?,
        next: String?, ai: Boolean
    ) {
        viewModelScope.launch {
            // Define loading state manual pois estamos fazendo uploads assíncronos antes
            state = state.copy(isLoading = true)

            // Processa uploads sequencialmente
            val bgUrl = processUpload(bg, "dialogues/backgrounds") ?: ""
            val musicUrl = processUpload(music, "dialogues/music")
            val audioUrl = processUpload(audio, "dialogues/audio")

            repository.saveDialogue(
                null, txt, desc, bgUrl, musicUrl, states, effects, speaker, audioUrl, expects, mode, expectResp, choices, next, ai
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        fetchDialogues()
                        // isLoading será setado para false dentro do fetchDialogues
                    }
                    is Resource.Error -> {
                        state = state.copy(error = result.message, isLoading = false)
                    }
                    is Resource.Loading -> {
                        state = state.copy(isLoading = true)
                    }
                }
            }
        }
    }

    // Helper function para gerenciar File vs String
    private suspend fun processUpload(input: Any?, folder: String): String? {
        return when (input) {
            is File -> {
                val result = repository.uploadFile(input, folder).firstOrNull { it is Resource.Success }
                result?.data
            }
            is String -> input.ifBlank { null }
            else -> null
        }
    }
}