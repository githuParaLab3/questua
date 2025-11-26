package com.questua.app.domain.usecase.monetization

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.TransactionRecord
import com.questua.app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfirmPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    operator fun invoke(paymentIntentId: String): Flow<Resource<TransactionRecord>> {
        return repository.confirmPayment(paymentIntentId)
    }
}