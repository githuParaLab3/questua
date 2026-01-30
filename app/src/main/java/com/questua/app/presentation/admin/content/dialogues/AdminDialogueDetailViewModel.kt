package com.questua.app.presentation.admin.content.dialogues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.InputMode
import com.questua.app.domain.model.SceneDialogue
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminDialogueDetailState(
    val isLoading: Boolean = false,
    val dialogue: SceneDialogue? = null,
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
    }

    fun fetchDetails() {
        repository.getDialogues(query = dialogueId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val found = result.data?.find { it.id == dialogueId }
                    state.copy(
                        dialogue = found,
                        isLoading = false,
                        error = if (found == null) "Diálogo não encontrado" else null
                    )
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveDialogue(
        textContent: String,
        description: String,
        backgroundUrl: String,
        speakerCharacterId: String?,
        expectsUserResponse: Boolean,
        inputMode: InputMode,
        nextDialogueId: String?
    ) {
        repository.saveDialogue(
            id = dialogueId,
            textContent = textContent,
            description = description,
            backgroundUrl = backgroundUrl,
            speakerCharacterId = speakerCharacterId,
            expectsUserResponse = expectsUserResponse,
            inputMode = inputMode,
            nextDialogueId = nextDialogueId
        ).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    fetchDetails()
                    state.copy(isLoading = false)
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteDialogue() {
        repository.deleteDialogue(dialogueId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}