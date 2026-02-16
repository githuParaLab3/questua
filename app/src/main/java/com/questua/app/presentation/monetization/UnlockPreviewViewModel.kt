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
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnlockPreviewState(
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val showSuccessPopup: Boolean = false,
    val error: String? = null,
    val requirement: UnlockRequirement? = null,
    val products: List<Product> = emptyList(),
    val userLevel: Int = 0,
    val userId: String? = null,
    val clientSecret: String? = null,
    val isUnlocked: Boolean = false
)

@HiltViewModel
class UnlockPreviewViewModel @Inject constructor(
    private val getUnlockPreviewUseCase: GetUnlockPreviewUseCase,
    private val paymentRepository: PaymentRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockPreviewState())
    val state: StateFlow<UnlockPreviewState> = _state.asStateFlow()

    val contentId: String = checkNotNull(savedStateHandle["contentId"])
    val contentType: String = checkNotNull(savedStateHandle["contentType"])

    init {
        loadData()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { id ->
                _state.value = _state.value.copy(userId = id)
                if (!id.isNullOrBlank()) {
                    fetchUserStats(id)
                }
            }
        }
    }

    private fun fetchUserStats(userId: String) {
        viewModelScope.launch {
            getUserStatsUseCase(userId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(
                        userLevel = result.data?.gamificationLevel ?: 0
                    )
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getUnlockPreviewUseCase(contentId, contentType).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val isUnlockedNow = result.data == null

                        _state.value = _state.value.copy(
                            requirement = result.data,
                            isUnlocked = isUnlockedNow,
                            isLoading = false
                        )

                        if (!isUnlockedNow && result.data?.premiumAccess == true) {
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
                        _state.value = _state.value.copy(isLoading = true)
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

    fun initiatePurchase(productId: String) {
        val userId = state.value.userId ?: return
        val product = state.value.products.find { it.id == productId } ?: return

        viewModelScope.launch {
            paymentRepository.initiatePayment(
                userId = userId,
                productId = productId,
                amountCents = product.priceCents,
                currency = product.currency
            ).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isProcessingPayment = true, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isProcessingPayment = false,
                            clientSecret = result.data?.clientSecret
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isProcessingPayment = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        _state.value = _state.value.copy(clientSecret = null)

        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                _state.value = _state.value.copy(showSuccessPopup = true)
                startPollingForUnlock()
            }
            is PaymentSheetResult.Canceled -> {
                _state.value = _state.value.copy(isProcessingPayment = false)
            }
            is PaymentSheetResult.Failed -> {
                _state.value = _state.value.copy(
                    isProcessingPayment = false,
                    error = "Erro no pagamento: ${paymentResult.error.localizedMessage}"
                )
            }
        }
    }

    private fun startPollingForUnlock() {
        viewModelScope.launch {
            repeat(10) {
                delay(1500)

                getUnlockPreviewUseCase(contentId, contentType).collectLatest { result ->
                    if (result is Resource.Success && result.data == null) {
                        _state.value = _state.value.copy(isUnlocked = true)
                        return@collectLatest
                    }
                }

                if (_state.value.isUnlocked) return@launch
            }
        }
    }

    fun dismissSuccessPopup() {
        _state.value = _state.value.copy(showSuccessPopup = false)
    }
}