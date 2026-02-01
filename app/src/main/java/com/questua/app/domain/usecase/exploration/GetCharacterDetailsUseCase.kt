package com.questua.app.domain.usecase.exploration

import com.questua.app.core.common.Resource
import com.questua.app.domain.model.CharacterEntity
import com.questua.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharacterDetailsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(characterId: String): Flow<Resource<CharacterEntity>> {
        return repository.getCharacterDetails(characterId)
    }
}