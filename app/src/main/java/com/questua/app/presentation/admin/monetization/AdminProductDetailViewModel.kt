package com.questua.app.presentation.admin.monetization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Product
import com.questua.app.domain.usecase.admin.sales.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ProductDetailState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    var state by mutableStateOf(ProductDetailState())
        private set

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    init {
        loadProduct()
    }

    fun loadProduct() {
        getProductsUseCase().onEach { result ->
            state = when (result) {
                is Resource.Success -> {
                    val foundProduct = result.data?.find { it.id == productId }
                    state.copy(product = foundProduct, isLoading = false)
                }
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
                is Resource.Loading -> state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }
}