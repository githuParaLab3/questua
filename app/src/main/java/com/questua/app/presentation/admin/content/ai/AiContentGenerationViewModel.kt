package com.questua.app.presentation.admin.content.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AiContentType { QUEST, QUEST_POINT, SCENE_DIALOGUE, CHARACTER, ACHIEVEMENT }

@HiltViewModel
class AiContentGenerationViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AiGenerationState())
        private set

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        data class Success(val route: String) : NavigationEvent()
        data class Error(val message: String) : NavigationEvent()
    }

    fun onTypeSelected(type: AiContentType) {
        state = state.copy(selectedType = type)
    }

    fun onFieldUpdate(field: String, value: String) {
        state = state.copy(fields = state.fields.toMutableMap().apply { put(field, value) })
    }

    fun generate() {
        viewModelScope.launch {
            val flow: Flow<Resource<out Any>> = when (state.selectedType) {
                AiContentType.QUEST_POINT -> repository.generateQuestPoint(
                    cityId = state.fields["cityId"] ?: "",
                    theme = state.fields["theme"] ?: ""
                )
                AiContentType.QUEST -> repository.generateQuest(
                    questPointId = state.fields["questPointId"] ?: "",
                    context = state.fields["context"] ?: "",
                    difficulty = state.fields["difficulty"]?.toIntOrNull() ?: 1
                )
                AiContentType.SCENE_DIALOGUE -> repository.generateDialogue(
                    speakerId = state.fields["speakerId"] ?: "",
                    context = state.fields["context"] ?: "",
                    questId = state.fields["questId"],
                    inputMode = state.fields["inputMode"] ?: "CHOICE"
                )
                AiContentType.CHARACTER -> repository.generateCharacter(
                    archetype = state.fields["archetype"] ?: ""
                )
                AiContentType.ACHIEVEMENT -> repository.generateAchievement(
                    trigger = state.fields["trigger"] ?: "",
                    difficulty = state.fields["difficulty"] ?: "EASY"
                )
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> {
                        state = state.copy(isLoading = false)
                        processSuccess(result.data)
                    }
                    is Resource.Error -> {
                        state = state.copy(isLoading = false)
                        _navigationEvent.emit(NavigationEvent.Error(result.message ?: "Falha na geração"))
                    }
                }
            }
        }
    }

    private suspend fun processSuccess(data: Any?) {
        if (data == null) return

        val route = when (data) {
            is QuestPoint -> "quest_point/${data.id}"
            is Quest -> "quest/${data.id}"
            is CharacterEntity -> "character/${data.id}"
            is SceneDialogue -> "dialogue/${data.id}"
            is Achievement -> "achievement/${data.id}"
            else -> ""
        }

        if (route.isNotEmpty()) {
            _navigationEvent.emit(NavigationEvent.Success(route))
        }
    }
}

data class AiGenerationState(
    val selectedType: AiContentType = AiContentType.QUEST_POINT,
    val fields: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false
)