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
import com.questua.app.domain.model.SpriteSheet
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
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
        repository.getCharacters(null).onEach { result ->
            if (result is Resource.Success) {
                val found = result.data?.find { it.id == characterId }
                state = state.copy(character = found, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveCharacter(
        name: String, avatar: Any?, voice: Any?,
        sprites: List<Any>, persona: Persona?, isAi: Boolean
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // Reutilizando lógica de upload (pode ser abstraída em UseCase no futuro)
            var finalAvatarUrl: String = (avatar as? String) ?: ""
            if (avatar is File) {
                repository.uploadFile(avatar, "avatars").collect { if (it is Resource.Success) finalAvatarUrl = it.data!! }
            }

            var finalVoiceUrl: String? = voice as? String
            if (voice is File) {
                repository.uploadFile(voice, "voices").collect { if (it is Resource.Success) finalVoiceUrl = it.data }
            }

            val finalSpriteUrls = sprites.map { item ->
                async {
                    if (item is File) {
                        var url = ""
                        repository.uploadFile(item, "sprites").collect { if (it is Resource.Success) url = it.data!! }
                        url
                    } else item as String
                }
            }.awaitAll().filter { it.isNotEmpty() }

            val spriteSheet = if (finalSpriteUrls.isNotEmpty()) SpriteSheet(finalSpriteUrls) else null

            if (finalAvatarUrl.isNotEmpty()) {
                repository.saveCharacter(characterId, name, finalAvatarUrl, finalVoiceUrl, spriteSheet, persona, isAi)
                    .collect { result ->
                        if (result is Resource.Success) fetchDetails()
                        else state = state.copy(error = result.message, isLoading = false)
                    }
            }
        }
    }

    fun deleteCharacter() {
        repository.deleteCharacter(characterId).onEach { result ->
            if (result is Resource.Success) state = state.copy(isDeleted = true)
        }.launchIn(viewModelScope)
    }
}