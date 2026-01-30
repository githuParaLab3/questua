package com.questua.app.presentation.admin.content.cities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminCityDetailState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AdminCityDetailViewModel @Inject constructor(
    private val repository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(AdminCityDetailState())
        private set

    private val cityId: String = checkNotNull(savedStateHandle["cityId"])

    init {
        fetchCityDetails()
    }

    fun fetchCityDetails() {
        repository.getCities(query = cityId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success<List<City>> -> {
                    val foundCity = result.data?.find { it.id == cityId }
                    state.copy(
                        city = foundCity,
                        isLoading = false,
                        error = if (foundCity == null) "Cidade não encontrada" else null
                    )
                }
                is Resource.Error<List<City>> -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveCity(
        id: String?,
        name: String,
        code: String,
        desc: String,
        langId: String,
        lat: Double,
        lon: Double,
        url: String?
    ) {
        repository.saveCity(id, name, code, desc, langId, lat, lon, url).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success -> {
                    fetchCityDetails()
                    state.copy(isLoading = false)
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteCity() {
        repository.deleteCity(cityId).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true)
                is Resource.Success<Unit> -> state.copy(isDeleted = true, isLoading = false)
                is Resource.Error<Unit> -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }
}