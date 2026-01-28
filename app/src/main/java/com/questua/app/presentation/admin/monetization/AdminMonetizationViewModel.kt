package com.questua.app.presentation.admin.monetization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.TargetType
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.TransactionRecord
import com.questua.app.domain.usecase.admin.sales.CreateProductUseCase
import com.questua.app.domain.usecase.admin.sales.DeleteProductUseCase
import com.questua.app.domain.usecase.admin.sales.GetProductsUseCase
import com.questua.app.domain.usecase.admin.sales.GetTransactionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MonetizationState(
    val products: List<Product> = emptyList(),
    val transactions: List<TransactionRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateModal: Boolean = false
)

@HiltViewModel
class AdminMonetizationViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase
) : ViewModel() {

    var state by mutableStateOf(MonetizationState())
        private set

    init {
        loadData()
    }

    fun loadData() {
        fetchProducts()
        fetchTransactions()
    }

    private fun fetchProducts() {
        getProductsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state = state.copy(products = result.data ?: emptyList(), isLoading = false)
                }
                is Resource.Error -> {
                    state = state.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchTransactions() {
        getTransactionHistoryUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    state = state.copy(transactions = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    // Log error opcional
                }
                is Resource.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun toggleCreateModal(show: Boolean) {
        state = state.copy(showCreateModal = show)
    }

    fun createProduct(
        sku: String,
        title: String,
        description: String,
        priceCents: Int,
        targetType: TargetType,
        targetId: String
    ) {
        val newProduct = Product(
            id = "", // Backend gera
            sku = sku,
            title = title,
            description = description,
            priceCents = priceCents,
            currency = "BRL",
            targetType = targetType,
            targetId = targetId,
            createdAt = "" // Backend gera
        )

        createProductUseCase(newProduct).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    toggleCreateModal(false)
                    fetchProducts()
                }
                is Resource.Error -> {
                    state = state.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun deleteProduct(id: String) {
        deleteProductUseCase(id).onEach { result ->
            when (result) {
                is Resource.Success -> fetchProducts()
                is Resource.Error -> state = state.copy(error = result.message)
                is Resource.Loading -> state = state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }
}