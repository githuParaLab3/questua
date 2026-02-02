package com.questua.app.presentation.admin.content.characters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.model.Persona
import com.questua.app.domain.model.SpriteSheet
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
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

    init {
        fetchCharacters()
    }

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
        avatar: Any?, // File ou String
        voice: Any?,  // File ou String
        sprites: List<Any>, // Lista de File ou String
        persona: Persona?,
        isAi: Boolean
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // 1. Upload Avatar
            var finalAvatarUrl: String = ""
            if (avatar is File) {
                repository.uploadFile(avatar, "avatars").collect {
                    if (it is Resource.Success) finalAvatarUrl = it.data ?: ""
                }
            } else if (avatar is String) {
                finalAvatarUrl = avatar
            }

            // 2. Upload Voice
            var finalVoiceUrl: String? = null
            if (voice is File) {
                repository.uploadFile(voice, "voices").collect {
                    if (it is Resource.Success) finalVoiceUrl = it.data
                }
            } else if (voice is String) {
                finalVoiceUrl = voice
            }

            // 3. Upload Sprites (Paralelo)
            val finalSpriteUrls = sprites.map { item ->
                async {
                    if (item is File) {
                        var url = ""
                        repository.uploadFile(item, "sprites").collect {
                            if (it is Resource.Success) url = it.data ?: ""
                        }
                        url
                    } else {
                        item as String
                    }
                }
            }.awaitAll().filter { it.isNotEmpty() }

            val spriteSheet = if (finalSpriteUrls.isNotEmpty()) SpriteSheet(finalSpriteUrls) else null

            // 4. Salvar Entidade
            if (finalAvatarUrl.isNotEmpty()) {
                repository.saveCharacter(id, name, finalAvatarUrl, finalVoiceUrl, spriteSheet, persona, isAi)
                    .collect { result ->
                        if (result is Resource.Success) fetchCharacters()
                        else if (result is Resource.Error) state = state.copy(error = result.message, isLoading = false)
                    }
            } else {
                state = state.copy(error = "Erro no upload do Avatar", isLoading = false)
            }
        }
    }
}