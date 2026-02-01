package com.questua.app.presentation.admin.content.cities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.*
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AdminCityDetailState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val languages: List<Language> = emptyList(),
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminCityDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val languageRepository: LanguageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminCityDetailState())
        private set

    private val cityId: String? = savedStateHandle["cityId"]

    init {
        cityId?.let { refresh(it) }
    }

    fun refresh(id: String = cityId!!) {
        fetchCityDetails(id)
        fetchLanguages()
    }

    private fun fetchCityDetails(id: String) {
        adminRepository.getCities(query = id).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    val found = result.data?.find { it.id == id }
                    state.copy(city = found, isLoading = false, error = if (found == null) "Cidade não encontrada" else null)
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun fetchLanguages() {
        languageRepository.getAvailableLanguages().onEach { result ->
            if (result is Resource.Success) state = state.copy(languages = result.data ?: emptyList())
        }.launchIn(viewModelScope)
    }

    fun updateCity(
        cityName: String, countryCode: String, descriptionCity: String,
        languageId: String, boundingPolygon: BoundingPolygon?, lat: Double, lon: Double,
        imageFile: File?, iconFile: File?, isPremium: Boolean,
        unlockRequirement: UnlockRequirement?, isAiGenerated: Boolean, isPublished: Boolean
    ) {
        viewModelScope.launch {
            var finalImageUrl = state.city?.imageUrl
            var finalIconUrl = state.city?.iconUrl

            imageFile?.let {
                adminRepository.uploadFile(it, "cities").collect { res -> if (res is Resource.Success) finalImageUrl = res.data }
            }
            iconFile?.let {
                adminRepository.uploadFile(it, "icons").collect { res -> if (res is Resource.Success) finalIconUrl = res.data }
            }

            adminRepository.saveCity(
                cityId, cityName, countryCode, descriptionCity, languageId,
                boundingPolygon, lat, lon, finalImageUrl, finalIconUrl, isPremium,
                unlockRequirement, isAiGenerated, isPublished
            ).collect { result ->
                if (result is Resource.Success) fetchCityDetails(cityId!!)
            }
        }
    }

    fun deleteCity() {
        cityId?.let { id ->
            adminRepository.deleteCity(id).onEach { result ->
                if (result is Resource.Success) state = state.copy(isDeleted = true)
            }.launchIn(viewModelScope)
        }
    }
}