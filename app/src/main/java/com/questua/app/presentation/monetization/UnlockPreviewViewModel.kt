package com.questua.app.presentation.monetization

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.UnlockRequirement
import com.questua.app.domain.repository.PaymentRepository
import com.questua.app.domain.usecase.exploration.GetUnlockPreviewUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnlockPreviewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val requirement: UnlockRequirement? = null,
    val products: List<Product> = emptyList(),
    val userLevel: Int = 0,
    val isPaymentProcessing: Boolean = false,
    val purchaseSuccess: Boolean = false
)

@HiltViewModel
class UnlockPreviewViewModel @Inject constructor(
    private val getUnlockPreviewUseCase: GetUnlockPreviewUseCase,
    private val paymentRepository: PaymentRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val tokenManager: TokenManager, // Injetamos o TokenManager para pegar o ID
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockPreviewState())
    val state: StateFlow<UnlockPreviewState> = _state.asStateFlow()

    private val contentId: String = checkNotNull(savedStateHandle["contentId"])
    private val contentType: String = checkNotNull(savedStateHandle["contentType"])

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1. Busca os requisitos de desbloqueio do conteúdo
            launch {
                getUnlockPreviewUseCase(contentId, contentType).collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                requirement = result.data,
                                isLoading = false
                            )
                            // Se requer acesso premium, busca os produtos vinculados
                            if (result.data?.premiumAccess == true) {
                                fetchProducts()
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message,
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Mantém loading true
                        }
                    }
                }
            }

            // 2. Busca stats do usuário (UserLanguage) usando o ID armazenado
            launch {
                tokenManager.userId.collectLatest { userId ->
                    if (!userId.isNullOrBlank()) {
                        getUserStatsUseCase(userId).collectLatest { result ->
                            if (result is Resource.Success) {
                                // Atualiza o nível do usuário para comparação na UI
                                _state.value = _state.value.copy(
                                    userLevel = result.data?.gamificationLevel ?: 0
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            paymentRepository.getProducts(contentId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(products = result.data ?: emptyList())
                }
            }
        }
    }

    fun purchaseProduct(product: Product) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPaymentProcessing = true)

            // Pega o userId atual para iniciar o pagamento
            tokenManager.userId.collectLatest { userId ->
                if (userId != null) {
                    paymentRepository.initiatePayment(userId, product.id, product.priceCents, product.currency)
                        .collectLatest { result ->
                            when (result) {
                                is Resource.Success -> {
                                    // Sucesso no início do pagamento, confirma a transação
                                    confirmPurchase(result.data?.clientSecret ?: "")
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        error = result.message,
                                        isPaymentProcessing = false
                                    )
                                }
                                is Resource.Loading -> {}
                            }
                        }
                }
            }
        }
    }

    private fun confirmPurchase(intentId: String) {
        viewModelScope.launch {
            paymentRepository.confirmPayment(intentId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(
                        isPaymentProcessing = false,
                        purchaseSuccess = true
                    )
                } else if (result is Resource.Error) {
                    _state.value = _state.value.copy(
                        isPaymentProcessing = false,
                        error = result.message
                    )
                }
            }
        }
    }
}