package com.questua.app.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class QuestResultState(
    val questId: String = "",
    val xpEarned: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Int = 0
)

@HiltViewModel
class QuestResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(QuestResultState())
    val state: StateFlow<QuestResultState> = _state.asStateFlow()

    init {
        val questId: String = checkNotNull(savedStateHandle["questId"])
        val xpEarned: Int = checkNotNull(savedStateHandle["xpEarned"])
        val correctAnswers: Int = checkNotNull(savedStateHandle["correctAnswers"])
        val totalQuestions: Int = checkNotNull(savedStateHandle["totalQuestions"])

        val accuracy = if (totalQuestions > 0) {
            ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
        } else 0

        _state.value = QuestResultState(
            questId = questId,
            xpEarned = xpEarned,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            accuracy = accuracy
        )
    }
}