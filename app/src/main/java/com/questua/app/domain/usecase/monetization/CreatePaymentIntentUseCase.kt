package com.questua.app.domain.usecase.monetization

import com.questua.app.core.common.Resource
import com.questua.app.data.remote.dto.PaymentResponseDTO
import com.questua.app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreatePaymentIntentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    operator fun invoke(userId: String, productId: String, amount: Int, currency: String): Flow<Resource<PaymentResponseDTO>> {
        return repository.initiatePayment(userId, productId, amount, currency)
    }
}