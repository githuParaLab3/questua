package com.questua.app.domain.usecase.admin.sales

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Product
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Resource<List<Product>>> {
        return repository.getProducts(page, size)
    }
}