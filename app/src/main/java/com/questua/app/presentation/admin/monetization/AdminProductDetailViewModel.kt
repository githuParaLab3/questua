package com.questua.app.presentation.admin.monetization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.TargetType
import com.questua.app.domain.model.Product
import com.questua.app.domain.usecase.admin.sales.DeleteProductUseCase
import com.questua.app.domain.usecase.admin.sales.GetProductsUseCase
import com.questua.app.domain.usecase.admin.sales.UpdateProductUseCase
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
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
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

    fun deleteProduct(onSuccess: () -> Unit) {
        deleteProductUseCase(productId).onEach { result ->
            if (result is Resource.Success) onSuccess()
        }.launchIn(viewModelScope)
    }

    fun saveProduct(
        sku: String, title: String, description: String,
        priceCents: Int, currency: String, targetType: TargetType, targetId: String
    ) {
        val currentProduct = state.product ?: return
        val updatedProduct = currentProduct.copy(
            sku = sku, title = title, description = description,
            priceCents = priceCents, currency = currency,
            targetType = targetType, targetId = targetId
        )

        updateProductUseCase(updatedProduct).onEach { result ->
            if (result is Resource.Success) loadProduct()
        }.launchIn(viewModelScope)
    }
}