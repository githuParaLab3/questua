package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.data.remote.dto.PaymentResponseDTO
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getProducts(targetId: String): Flow<Resource<List<Product>>>
    fun getProductById(productId: String): Flow<Resource<Product>>
    fun initiatePayment(userId: String, productId: String, amountCents: Int, currency: String): Flow<Resource<PaymentResponseDTO>>
    fun confirmPayment(paymentIntentId: String): Flow<Resource<TransactionRecord>>
    fun getTransactionHistory(userId: String): Flow<Resource<List<TransactionRecord>>>
    fun updateProductPrice(productId: String, newPrice: Int): Flow<Resource<Product>>
}