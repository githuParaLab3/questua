package com.questua.app.domain.usecase.admin.selectors

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.City
import com.questua.app.domain.repository.AdminRepository // Ou CityRepository se preferir acesso direto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCitiesSelectorUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(): Flow<Resource<List<City>>> {
        // Reutilizando o método de listar cidades do admin, mas poderia ser um específico 'listAllSimple'
        return repository.getAllCities(page = 0, size = 100)
    }
}