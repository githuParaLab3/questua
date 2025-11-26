package com.questua.app.domain.repository

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getProducts(targetId: String): Flow<Resource<List<Product>>>
    fun createPaymentIntent(productId: String): Flow<Resource<String>>
    fun confirmPayment(paymentIntentId: String): Flow<Resource<TransactionRecord>>
    fun getTransactionHistory(userId: String): Flow<Resource<List<TransactionRecord>>>
    fun updateProductPrice(productId: String, newPrice: Int): Flow<Resource<Product>>
}