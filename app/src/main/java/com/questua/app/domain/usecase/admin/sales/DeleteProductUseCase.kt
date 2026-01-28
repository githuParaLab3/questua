package com.questua.app.domain.usecase.admin.sales

import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(productId: String): Flow<Resource<Unit>> {
        return repository.deleteProduct(productId)
    }
}