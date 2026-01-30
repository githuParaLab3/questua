package com.questua.app.presentation.admin.content.characters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Persona
import com.questua.app.domain.repository.AdminRepository // Ou seu repositório de personagens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminCharacterState(
    val isLoading: Boolean = false,
    val characters: List<CharacterEntity> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminCharacterViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminCharacterState())
        private set

    private var searchJob: Job? = null

    init { fetchCharacters() }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchCharacters()
        }
    }

    fun fetchCharacters() {
        repository.getCharacters(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.characters.isEmpty())
                is Resource.Success -> state.copy(characters = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveCharacter(
        id: String?,
        name: String,
        avatarUrl: String,
        isAi: Boolean,
        voiceUrl: String?,
        persona: Persona?
    ) {
        repository.saveCharacter(id, name, avatarUrl, isAi, voiceUrl, persona).onEach { result ->
            when (result) {
                is Resource.Success -> fetchCharacters()
                is Resource.Error -> state = state.copy(error = result.message)
                is Resource.Loading -> state = state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteCharacter(id: String) {
        repository.deleteCharacter(id).onEach { result ->
            if (result is Resource.Success) fetchCharacters()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}