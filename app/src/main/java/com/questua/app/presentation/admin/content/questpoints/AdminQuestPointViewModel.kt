// fileName: app/src/main/java/com/questua/app/presentation/admin/content/questpoints/AdminQuestPointViewModel.kt
package com.questua.app.presentation.admin.content.questpoints

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint
import com.questua.app.domain.model.UnlockRequirement
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminQuestPointState(
    val isLoading: Boolean = false,
    val points: List<QuestPoint> = emptyList(),
    val cities: List<City> = emptyList(), // Adicionado para o seletor
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

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchPoints()
        fetchCities()
    }

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

    fun fetchCities() {
        // Assume que AdminRepository tem getCities, como visto no AdminCityViewModel
        repository.getCities(null).onEach { result ->
            if (result is Resource.Success) {
                state = state.copy(cities = result.data ?: emptyList())
            }
        }.launchIn(viewModelScope)
    }

    fun savePoint(
        id: String?, cityId: String, title: String, desc: String,
        difficulty: Int, lat: Double, lon: Double,
        imageFile: File?, iconFile: File?,
        unlockRequirement: UnlockRequirement?,
        isPremium: Boolean, isAiGenerated: Boolean, isPublished: Boolean
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            var finalImageUrl: String? = null
            var finalIconUrl: String? = null

            // 1. Upload das imagens se houver novos arquivos
            if (imageFile != null) {
                repository.uploadFile(imageFile, "quest_points").collect { res ->
                    if (res is Resource.Success) finalImageUrl = res.data
                }
            }
            if (iconFile != null) {
                repository.uploadFile(iconFile, "icons").collect { res ->
                    if (res is Resource.Success) finalIconUrl = res.data
                }
            }

            // 2. Salvar o objeto completo
            // NOTA: Você precisará atualizar o Repository para aceitar esses parâmetros extras se ainda não aceitar
            repository.saveQuestPoint(
                id = id,
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
                    fetchPoints()
                } else if (result is Resource.Error) {
                    state = state.copy(error = result.message, isLoading = false)
                }
            }
        }
    }

    fun deletePoint(id: String) {
        repository.deleteQuestPoint(id).onEach { result ->
            if (result is Resource.Success) fetchPoints()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}