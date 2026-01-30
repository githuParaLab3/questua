package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminQuestPointDetailState(
    val isLoading: Boolean = false,
    val questPoint: QuestPoint? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminQuestPointDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminQuestPointDetailState())
        private set

    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    init {
        fetchDetails()
    }

    fun fetchDetails() {
        repository.getQuestPoints(query = pointId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val found = result.data?.find { it.id == pointId }
                    state.copy(
                        questPoint = found,
                        isLoading = false,
                        error = if (found == null) "Quest Point não encontrado" else null
                    )
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveQuestPoint(
        name: String,
        cityId: String,
        lat: Double,
        lon: Double,
        desc: String
    ) {
        // A ordem correta conforme o AdminRepository: (id, cityId, title, description, lat, lon)
        repository.saveQuestPoint(
            id = pointId,
            cityId = cityId,
            title = name,
            description = desc,
            lat = lat,
            lon = lon
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

    fun deleteQuestPoint() {
        repository.deleteQuestPoint(pointId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}