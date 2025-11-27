package com.questua.app.presentation.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.ReportType
import com.questua.app.domain.usecase.feedback.SendReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FeedbackState(
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val sendReportUseCase: SendReportUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackState())
    val state = _state.asStateFlow()

    val description = MutableStateFlow("")
    val screenshotUri = MutableStateFlow<Uri?>(null)

    private var screenshotFile: File? = null

    val reportType: ReportType = try {
        ReportType.valueOf(savedStateHandle.get<String>("reportType") ?: ReportType.FEEDBACK.name)
    } catch (e: IllegalArgumentException) {
        ReportType.FEEDBACK
    }

    fun onImageSelected(file: File, uri: Uri) {
        screenshotFile = file
        screenshotUri.value = uri
    }

    fun clearImage() {
        screenshotFile = null
        screenshotUri.value = null
    }

    fun sendReport() {
        if (description.value.isBlank()) {
            _state.value = _state.value.copy(error = "A descrição não pode estar vazia.")
            return
        }

        viewModelScope.launch {
            val userId = tokenManager.userId.firstOrNull()

            if (userId.isNullOrEmpty()) {
                _state.value = _state.value.copy(error = "Sessão expirada. Faça login novamente.")
                return@launch
            }

            sendReportUseCase(
                userId = userId,
                type = reportType,
                description = description.value,
                screenshotFile = screenshotFile
            ).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
                    is Resource.Success -> {
                        val message = if (reportType == ReportType.ERROR) {
                            "Problema reportado com sucesso! Agradecemos sua ajuda."
                        } else {
                            "Sugestão enviada com sucesso! Analisaremos em breve."
                        }

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSent = true,
                            successMessage = message
                        )
                        description.value = ""
                        clearImage()
                    }
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}