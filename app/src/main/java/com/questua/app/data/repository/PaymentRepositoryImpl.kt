package com.questua.app.data.repository

import com.questua.app.core.common.Resource
import com.questua.app.core.network.SafeApiCall
import com.questua.app.data.mapper.toDomain
import com.questua.app.data.remote.api.PaymentApi
import com.questua.app.data.remote.api.ProductApi
import com.questua.app.data.remote.api.TransactionRecordApi
import com.questua.app.data.remote.dto.PaymentRequestDTO
import com.questua.app.data.remote.dto.PaymentResponseDTO
import com.questua.app.data.remote.dto.ProductRequestDTO
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.TransactionRecord
import com.questua.app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentApi: PaymentApi,
    private val productApi: ProductApi,
    private val transactionApi: TransactionRecordApi
) : PaymentRepository, SafeApiCall() {

    override fun getProducts(targetId: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        // Assumindo filtro por targetId se backend suportar, ou lista tudo
        val result = safeApiCall { productApi.list(filter = mapOf("targetId" to targetId)) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.content.map { it.toDomain() }))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar produtos"))
        }
    }

    override fun getProductById(productId: String): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { productApi.getById(productId) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!.toDomain()))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar produto"))
        }
    }

    override fun initiatePayment(
        userId: String,
        productId: String,
        amountCents: Int,
        currency: String
    ): Flow<Resource<PaymentResponseDTO>> = flow {
        emit(Resource.Loading())
        val request = PaymentRequestDTO(userId, productId, amountCents, currency)
        val result = safeApiCall { paymentApi.initiatePayment(request) }
        if (result is Resource.Success) {
            emit(Resource.Success(result.data!!))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao iniciar pagamento"))
        }
    }

    override fun getTransactionHistory(userId: String): Flow<Resource<List<TransactionRecord>>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall { transactionApi.list() }
        if (result is Resource.Success) {
            val all = result.data!!.content.map { it.toDomain() }
            // Filtragem client-side por segurança caso a API retorne tudo
            val userTransactions = all.filter { it.userId == userId }
            emit(Resource.Success(userTransactions))
        } else {
            emit(Resource.Error(result.message ?: "Erro ao carregar transações"))
        }
    }

    override fun confirmPayment(paymentIntentId: String): Flow<Resource<TransactionRecord>> = flow {
        emit(Resource.Loading())
        val result = safeApiCall {
            transactionApi.list(filter = mapOf("stripePaymentIntentId" to paymentIntentId))
        }
        if (result is Resource.Success && result.data!!.content.isNotEmpty()) {
            emit(Resource.Success(result.data!!.content.first().toDomain()))
        } else {
            emit(Resource.Error("Transação não encontrada"))
        }
    }

    override fun updateProductPrice(productId: String, newPrice: Int): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        val get = safeApiCall { productApi.getById(productId) }
        if (get is Resource.Success) {
            val current = get.data!!
            val req = ProductRequestDTO(
                sku = current.sku,
                title = current.title,
                descriptionProduct = current.descriptionProduct,
                priceCents = newPrice,
                currency = current.currency,
                targetType = current.targetType,
                targetId = current.targetId
            )
            val update = safeApiCall { productApi.update(productId, req) }
            if (update is Resource.Success) emit(Resource.Success(update.data!!.toDomain()))
            else emit(Resource.Error("Falha ao atualizar"))
        } else {
            emit(Resource.Error("Produto não encontrado"))
        }
    }
}