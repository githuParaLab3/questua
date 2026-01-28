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
import com.questua.app.domain.usecase.admin.selectors.GetCitiesSelectorUseCase
import com.questua.app.domain.usecase.admin.selectors.GetQuestPointsSelectorUseCase
import com.questua.app.domain.usecase.admin.selectors.GetQuestsSelectorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class SelectorItem(
    val id: String,
    val name: String,
    val detail: String
)

data class MonetizationState(
    val products: List<Product> = emptyList(),
    val transactions: List<TransactionRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateModal: Boolean = false,

    val selectorItems: List<SelectorItem> = emptyList(),
    val showTargetSelector: Boolean = false,
    val selectedTargetName: String? = null
)

@HiltViewModel
class AdminMonetizationViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    private val getCitiesSelectorUseCase: GetCitiesSelectorUseCase,
    private val getQuestsSelectorUseCase: GetQuestsSelectorUseCase,
    private val getQuestPointsSelectorUseCase: GetQuestPointsSelectorUseCase
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
            state = when (result) {
                is Resource.Success -> state.copy(products = result.data ?: emptyList(), isLoading = false)
                is Resource.Error -> state.copy(error = result.message, isLoading = false)
                is Resource.Loading -> state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchTransactions() {
        getTransactionHistoryUseCase().onEach { result ->
            if (result is Resource.Success) {
                state = state.copy(transactions = result.data ?: emptyList())
            }
        }.launchIn(viewModelScope)
    }

    fun toggleCreateModal(show: Boolean) {
        state = state.copy(
            showCreateModal = show,
            selectedTargetName = null // Reset selection on open/close
        )
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
            id = "",
            sku = sku,
            title = title,
            description = description,
            priceCents = priceCents,
            currency = "BRL",
            targetType = targetType,
            targetId = targetId,
            createdAt = ""
        )

        createProductUseCase(newProduct).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    toggleCreateModal(false)
                    fetchProducts()
                }
                is Resource.Error -> state = state.copy(error = result.message, isLoading = false)
                is Resource.Loading -> state = state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteProduct(id: String) {
        deleteProductUseCase(id).onEach { result ->
            when (result) {
                is Resource.Success -> fetchProducts()
                is Resource.Error -> state = state.copy(error = result.message, isLoading = false)
                is Resource.Loading -> state = state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    fun openTargetSelector(targetType: TargetType) {
        state = state.copy(isLoading = true)

        val flow = when (targetType) {
            TargetType.CITY -> getCitiesSelectorUseCase().onEach { res -> mapToSelector(res, { it.id }, { it.name }, { it.countryCode }) }
            TargetType.QUEST -> getQuestsSelectorUseCase().onEach { res -> mapToSelector(res, { it.id }, { it.title }, { "Dif: ${it.difficulty}" }) }
            TargetType.QUEST_POINT -> getQuestPointsSelectorUseCase().onEach { res -> mapToSelector(res, { it.id }, { it.title }, { "Dif: ${it.difficulty}" }) }
            else -> getCitiesSelectorUseCase().onEach { }
        }

        flow.launchIn(viewModelScope)
    }

    private fun <T> mapToSelector(
        result: Resource<List<T>>,
        idMapper: (T) -> String,
        nameMapper: (T) -> String,
        detailMapper: (T) -> String
    ) {
        state = when (result) {
            is Resource.Success -> {
                val items = result.data?.map {
                    SelectorItem(idMapper(it), nameMapper(it), detailMapper(it))
                } ?: emptyList()
                state.copy(selectorItems = items, isLoading = false, showTargetSelector = true)
            }
            is Resource.Error -> state.copy(error = result.message, isLoading = false)
            is Resource.Loading -> state.copy(isLoading = true)
        }
    }

    fun closeTargetSelector() {
        state = state.copy(showTargetSelector = false)
    }
}