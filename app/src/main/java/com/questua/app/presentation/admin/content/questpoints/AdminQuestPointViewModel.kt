package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminQuestPointState(
    val isLoading: Boolean = false,
    val points: List<QuestPoint> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminQuestPointViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    var state by mutableStateOf(AdminQuestPointState())
        private set

    private var searchJob: Job? = null

    init { fetchPoints() }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchPoints()
        }
    }

    fun fetchPoints() {
        repository.getQuestPoints(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.points.isEmpty())
                is Resource.Success -> state.copy(points = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun savePoint(id: String?, cityId: String, title: String, desc: String, lat: Double, lon: Double) {
        repository.saveQuestPoint(id, cityId, title, desc, lat, lon).onEach { result ->
            if (result is Resource.Success) fetchPoints()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }

    fun deletePoint(id: String) {
        repository.deleteQuestPoint(id).onEach { result ->
            if (result is Resource.Success) fetchPoints()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}