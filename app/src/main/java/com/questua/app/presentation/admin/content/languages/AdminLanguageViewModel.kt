package com.questua.app.presentation.admin.content.languages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminLanguageState(
    val isLoading: Boolean = false,
    val languages: List<Language> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminLanguageViewModel @Inject constructor(
    private val repository: LanguageRepository
) : ViewModel() {

    var state by mutableStateOf(AdminLanguageState())
        private set

    private var searchJob: Job? = null

    init {
        fetchLanguages()
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchLanguages()
        }
    }

    fun fetchLanguages() {
        repository.getAvailableLanguages(state.searchQuery.takeIf { it.isNotBlank() }).onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = state.languages.isEmpty())
                is Resource.Success -> state.copy(languages = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun saveLanguage(id: String?, name: String, code: String) {
        val flow = if (id == null) repository.createLanguage(name, code)
        else repository.updateLanguage(id, name, code)

        flow.onEach { result ->
            if (result is Resource.Success) fetchLanguages()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }

    fun deleteLanguage(id: String) {
        repository.deleteLanguage(id).onEach { result ->
            if (result is Resource.Success) fetchLanguages()
            else if (result is Resource.Error) state = state.copy(error = result.message)
        }.launchIn(viewModelScope)
    }
}