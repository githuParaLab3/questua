// fileName: app/src/main/java/com/questua/app/presentation/admin/content/questpoints/AdminQuestPointDetailViewModel.kt
package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UnlockRequirement
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminQuestPointDetailState(
    val isLoading: Boolean = false,
    val questPoint: QuestPoint? = null,
    val cities: List<City> = emptyList(),
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
        fetchCities()
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

    fun fetchCities() {
        repository.getCities(null).onEach { result ->
            if (result is Resource.Success) {
                state = state.copy(cities = result.data ?: emptyList())
            }
        }.launchIn(viewModelScope)
    }

    fun saveQuestPoint(
        title: String, cityId: String, desc: String,
        difficulty: Int, lat: Double, lon: Double,
        imageFile: File?, iconFile: File?,
        unlockRequirement: UnlockRequirement?,
        isPremium: Boolean, isAiGenerated: Boolean, isPublished: Boolean
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            var finalImageUrl: String? = state.questPoint?.imageUrl
            var finalIconUrl: String? = state.questPoint?.iconUrl

            if (imageFile != null) {
                repository.uploadFile(imageFile, "quest_points").collect { if (it is Resource.Success) finalImageUrl = it.data }
            }
            if (iconFile != null) {
                repository.uploadFile(iconFile, "icons").collect { if (it is Resource.Success) finalIconUrl = it.data }
            }

            repository.saveQuestPoint(
                id = pointId,
                cityId = cityId,
                title = title,
                description = desc,
                difficulty = difficulty,
                lat = lat,
                lon = lon,
                imageUrl = finalImageUrl,
                iconUrl = finalIconUrl,
                unlockRequirement = unlockRequirement,
                isPremium = isPremium,
                isAiGenerated = isAiGenerated,
                isPublished = isPublished
            ).collect { result ->
                if (result is Resource.Success) {
                    fetchDetails()
                } else if (result is Resource.Error) {
                    state = state.copy(error = result.message, isLoading = false)
                }
            }
        }
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