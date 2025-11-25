package com.questua.app.domain.usecase.auth
import com.questua.app.domain.repository.AuthRepository
import javax.inject.Inject
class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(e: String, d: String, p: String, l: String) = repo.register(e, d, p, l)
}