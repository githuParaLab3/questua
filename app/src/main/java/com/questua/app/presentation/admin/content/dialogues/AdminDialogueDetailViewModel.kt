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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminDialogueDetailState(
    val isLoading: Boolean = false,
    val dialogue: SceneDialogue? = null,
    val allDialogues: List<SceneDialogue> = emptyList(),
    val characters: List<CharacterEntity> = emptyList(),
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

    // Função atualizada para aceitar Any? (File ou String) nos campos de mídia
    fun saveDialogue(
        txt: String, desc: String, bg: Any?, music: Any?,
        states: List<CharacterState>?, effects: List<SceneEffect>?,
        speaker: String?, audio: Any?, expects: Boolean,
        mode: InputMode, expectResp: String?, choices: List<Choice>?,
        next: String?, ai: Boolean
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // Processa uploads sequencialmente se necessário
            val bgUrl = processUpload(bg, "dialogues/backgrounds") ?: ""
            val musicUrl = processUpload(music, "dialogues/music")
            val audioUrl = processUpload(audio, "dialogues/audio")

            // Se o background for obrigatório e veio vazio (erro no upload ou input vazio), você pode tratar aqui
            // Por enquanto, passamos para o repositório

            repository.saveDialogue(
                dialogueId, txt, desc, bgUrl, musicUrl, states, effects, speaker, audioUrl, expects, mode, expectResp, choices, next, ai
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        state = state.copy(isLoading = false)
                        fetchDetails() // Recarrega os dados atualizados
                    }
                    is Resource.Error -> {
                        state = state.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {
                        state = state.copy(isLoading = true)
                    }
                }
            }
        }
    }

    // Helper para decidir se faz upload ou usa a string existente
    private suspend fun processUpload(input: Any?, folder: String): String? {
        return when (input) {
            is File -> {
                // Se for arquivo, faz upload e aguarda o sucesso para pegar a URL
                val result = repository.uploadFile(input, folder).firstOrNull { it is Resource.Success }
                result?.data
            }
            is String -> input.ifBlank { null } // Retorna a URL ou null se vazio
            else -> null
        }
    }

    fun deleteDialogue() {
        repository.deleteDialogue(dialogueId).onEach { result ->
            if (result is Resource.Success) state = state.copy(isDeleted = true)
        }.launchIn(viewModelScope)
    }
}