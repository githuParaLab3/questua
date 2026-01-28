package com.questua.app.domain.usecase.admin.sales

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.TransactionRecord
import com.questua.app.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionHistoryUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    /**
     * Busca o histórico de transações do sistema.
     *
     * @param page Número da página (zero-based).
     * @param size Quantidade de itens por página.
     */
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Resource<List<TransactionRecord>>> {
        return repository.getAllTransactions(page, size)
    }
}