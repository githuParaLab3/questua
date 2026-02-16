package com.questua.app.presentation.monetization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Product
import com.questua.app.domain.repository.PaymentRepository
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    var state by mutableStateOf(PaymentState())
        private set

    fun loadProduct(productId: String) {
        paymentRepository.getProductById(productId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
                is Resource.Success -> {
                    state = state.copy(
                        isLoading = false,
                        product = result.data
                    )
                }
                is Resource.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun initiatePayment(userId: String) {
        val product = state.product ?: return

        paymentRepository.initiatePayment(
            userId = userId,
            productId = product.id,
            amountCents = product.priceCents,
            currency = product.currency
        ).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    state = state.copy(isProcessingPayment = true, paymentError = null)
                }
                is Resource.Success -> {
                    state = state.copy(
                        isProcessingPayment = false,
                        clientSecret = result.data?.clientSecret,
                        transactionId = result.data?.transactionId
                    )
                }
                is Resource.Error -> {
                    state = state.copy(
                        isProcessingPayment = false,
                        paymentError = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onPaymentResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                state = state.copy(paymentStatus = PaymentStatus.SUCCESS)
            }
            is PaymentSheetResult.Canceled -> {
                state = state.copy(paymentStatus = PaymentStatus.CANCELED)
            }
            is PaymentSheetResult.Failed -> {
                state = state.copy(
                    paymentStatus = PaymentStatus.FAILED,
                    paymentError = paymentResult.error.localizedMessage
                )
            }
        }
    }

    fun resetPaymentState() {
        state = state.copy(
            clientSecret = null,
            paymentStatus = PaymentStatus.IDLE,
            paymentError = null
        )
    }
}

data class PaymentState(
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val product: Product? = null,
    val error: String? = null,
    val clientSecret: String? = null,
    val transactionId: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.IDLE,
    val paymentError: String? = null
)

enum class PaymentStatus {
    IDLE, SUCCESS, CANCELED, FAILED
}