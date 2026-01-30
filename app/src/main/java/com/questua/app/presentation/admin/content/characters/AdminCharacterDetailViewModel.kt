package com.questua.app.presentation.admin.content.characters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Persona
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminCharacterDetailState(
    val isLoading: Boolean = false,
    val character: CharacterEntity? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminCharacterDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminCharacterDetailState())
        private set

    private val characterId: String = checkNotNull(savedStateHandle["characterId"])

    init {
        fetchDetails()
    }

    fun fetchDetails() {
        repository.getCharacters(query = characterId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val found = result.data?.find { it.id == characterId }
                    state.copy(
                        character = found,
                        isLoading = false,
                        error = if (found == null) "Personagem não encontrado" else null
                    )
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveCharacter(
        name: String,
        avatarUrl: String,
        isAi: Boolean,
        voiceUrl: String?,
        persona: Persona?
    ) {
        repository.saveCharacter(characterId, name, avatarUrl, isAi, voiceUrl, persona).onEach { result ->
            if (result is Resource.Success) fetchDetails()
        }.launchIn(viewModelScope)
    }

    fun deleteCharacter() {
        repository.deleteCharacter(characterId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}