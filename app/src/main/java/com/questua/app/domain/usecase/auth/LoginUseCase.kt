package com.questua.app.domain.usecase.auth
import com.questua.app.domain.repository.AuthRepository
import javax.inject.Inject
class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(e: String, p: String) = repo.login(e, p)
}