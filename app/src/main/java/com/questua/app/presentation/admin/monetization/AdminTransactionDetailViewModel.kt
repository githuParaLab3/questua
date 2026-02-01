package com.questua.app.presentation.admin.monetization

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.data.remote.api.TransactionRecordApi
import com.questua.app.data.remote.dto.TransactionRecordResponseDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminTransactionDetailViewModel @Inject constructor(
    private val api: TransactionRecordApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf<Resource<TransactionRecordResponseDTO>>(Resource.Loading())
    val state: State<Resource<TransactionRecordResponseDTO>> = _state

    init {
        savedStateHandle.get<String>("transactionId")?.let { id ->
            getTransaction(id)
        }
    }

    private fun getTransaction(id: String) {
        viewModelScope.launch {
            try {
                val response = api.getById(id)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = Resource.Success(response.body()!!)
                } else {
                    _state.value = Resource.Error("Erro ao carregar transação")
                }
            } catch (e: Exception) {
                _state.value = Resource.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}