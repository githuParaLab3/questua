package com.questua.app.domain.usecase.admin.sales

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Product
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(product: Product): Flow<Resource<Product>> {
        return repository.updateProduct(product)
    }
}