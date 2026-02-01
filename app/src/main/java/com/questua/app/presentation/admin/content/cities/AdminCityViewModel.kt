package com.questua.app.presentation.admin.content.cities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminCityState(
    val isLoading: Boolean = false,
    val cities: List<City> = emptyList(),
    val languages: List<Language> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminCityViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val languageRepository: LanguageRepository
) : ViewModel() {

    var state by mutableStateOf(AdminCityState())
        private set

    private var searchJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchCities()
        fetchLanguages()
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchCities()
        }
    }

    fun fetchCities() {
        adminRepository.getCities(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> state.copy(cities = result.data ?: emptyList(), isLoading = false, error = null)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun fetchLanguages() {
        languageRepository.getAvailableLanguages().onEach { result ->
            if (result is Resource.Success) {
                state = state.copy(languages = result.data ?: emptyList())
            }
        }.launchIn(viewModelScope)
    }

    fun createCity(
        cityName: String, countryCode: String, descriptionCity: String,
        languageId: String, boundingPolygon: BoundingPolygon?, lat: Double, lon: Double,
        imageFile: File?, iconFile: File?, isPremium: Boolean,
        unlockRequirement: UnlockRequirement?, isAiGenerated: Boolean, isPublished: Boolean
    ) {
        viewModelScope.launch {
            var finalImageUrl: String? = null
            var finalIconUrl: String? = null

            imageFile?.let {
                adminRepository.uploadFile(it, "cities").collect { res -> if (res is Resource.Success) finalImageUrl = res.data }
            }
            iconFile?.let {
                adminRepository.uploadFile(it, "icons").collect { res -> if (res is Resource.Success) finalIconUrl = res.data }
            }

            adminRepository.saveCity(
                null, cityName, countryCode, descriptionCity, languageId,
                boundingPolygon, lat, lon, finalImageUrl, finalIconUrl, isPremium,
                unlockRequirement, isAiGenerated, isPublished
            ).collect { result ->
                if (result is Resource.Success) fetchCities()
            }
        }
    }
}