package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminDialogueDetailState(
    val isLoading: Boolean = false,
    val dialogue: SceneDialogue? = null,
    val allDialogues: List<SceneDialogue> = emptyList(), // Para edição
    val characters: List<CharacterEntity> = emptyList(), // Para edição
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminDialogueDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminDialogueDetailState())
        private set

    private val dialogueId: String = checkNotNull(savedStateHandle["dialogueId"])

    init {
        fetchDetails()
        fetchDependencies()
    }

    fun fetchDetails() {
        repository.getDialogues(null).onEach { result ->
            if (result is Resource.Success) {
                val found = result.data?.find { it.id == dialogueId }
                state = state.copy(dialogue = found, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchDependencies() {
        repository.getCharacters(null).onEach {
            if (it is Resource.Success) state = state.copy(characters = it.data ?: emptyList())
        }.launchIn(viewModelScope)

        repository.getDialogues(null).onEach {
            if (it is Resource.Success) state = state.copy(allDialogues = it.data ?: emptyList())
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
            dialogueId, txt, desc, bg, music, states, effects, speaker, audio, expects, mode, expectResp, choices, next, ai
        ).onEach { result ->
            if (result is Resource.Success) fetchDetails()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }

    fun deleteDialogue() {
        repository.deleteDialogue(dialogueId).onEach { result ->
            if (result is Resource.Success) state = state.copy(isDeleted = true)
        }.launchIn(viewModelScope)
    }
}