package com.questua.app.presentation.monetization

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.managers.AchievementMonitor
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.UnlockRequirement
import com.questua.app.domain.repository.AdminRepository
import com.questua.app.domain.repository.PaymentRepository
import com.questua.app.domain.usecase.exploration.GetUnlockPreviewUseCase
import com.questua.app.domain.usecase.language_learning.GetUserLanguagesUseCase
import com.questua.app.domain.usecase.user.GetUserAchievementsUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnlockPreviewState(
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val showSuccessPopup: Boolean = false,
    val error: String? = null,
    val requirement: UnlockRequirement? = null,
    val products: List<Product> = emptyList(),
    val pendingAchievements: List<Achievement> = emptyList(),
    val userLevel: Int = 0,
    val userId: String? = null,
    val clientSecret: String? = null,
    val isUnlocked: Boolean = false
)

@HiltViewModel
class UnlockPreviewViewModel @Inject constructor(
    private val getUnlockPreviewUseCase: GetUnlockPreviewUseCase,
    private val paymentRepository: PaymentRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserLanguagesUseCase: GetUserLanguagesUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager,
    private val achievementMonitor: AchievementMonitor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockPreviewState())
    val state: StateFlow<UnlockPreviewState> = _state.asStateFlow()

    val contentId: String = checkNotNull(savedStateHandle["contentId"])
    val contentType: String = checkNotNull(savedStateHandle["contentType"])

    init {
        loadData()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { id ->
                _state.value = _state.value.copy(userId = id)
                if (!id.isNullOrBlank()) {
                    fetchUserStats(id)
                }
            }
        }
    }

    private fun fetchUserStats(userId: String) {
        viewModelScope.launch {
            getUserStatsUseCase(userId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(
                        userLevel = result.data?.gamificationLevel ?: 0
                    )
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val userId = try {
                tokenManager.userId.first { !it.isNullOrEmpty() }
            } catch (e: Exception) { null }

            getUnlockPreviewUseCase(contentId, contentType).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            requirement = result.data,
                            isLoading = false
                        )
                        if (result.data?.premiumAccess == true) {
                            fetchProducts()
                        }
                        if (userId != null) {
                            loadMonetizationAchievements(userId)
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private suspend fun loadMonetizationAchievements(userId: String) {
        try {
            val allAchJob = viewModelScope.async {
                adminRepository.getAchievements(null).filter { it !is Resource.Loading }.first().data ?: emptyList()
            }
            val userAchJob = viewModelScope.async {
                getUserAchievementsUseCase(userId).filter { it !is Resource.Loading }.first().data ?: emptyList()
            }

            val all = allAchJob.await()
            val userAchIds = userAchJob.await().map { it.achievementId }.toSet()

            val related = all.filter { ach ->
                val isMonetizationType = ach.conditionType.name == "UNLOCK_PREMIUM_CONTENT"
                val isTargeted = ach.targetId == contentId
                (isMonetizationType || isTargeted) && !userAchIds.contains(ach.id)
            }.take(2)

            _state.value = _state.value.copy(pendingAchievements = related)
        } catch (e: Exception) { }
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            paymentRepository.getProducts(contentId).collectLatest { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(products = result.data ?: emptyList())
                }
            }
        }
    }

    fun initiatePurchase(productId: String) {
        val userId = state.value.userId ?: return
        val product = state.value.products.find { it.id == productId } ?: return

        viewModelScope.launch {
            paymentRepository.initiatePayment(
                userId = userId,
                productId = productId,
                amountCents = product.priceCents,
                currency = product.currency
            ).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isProcessingPayment = true, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isProcessingPayment = false,
                            clientSecret = result.data?.clientSecret
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isProcessingPayment = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        _state.value = _state.value.copy(clientSecret = null)

        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                _state.value = _state.value.copy(showSuccessPopup = true)
                startPollingForUnlock()
            }
            is PaymentSheetResult.Canceled -> {
                _state.value = _state.value.copy(isProcessingPayment = false)
            }
            is PaymentSheetResult.Failed -> {
                _state.value = _state.value.copy(
                    isProcessingPayment = false,
                    error = "Erro no pagamento"
                )
            }
        }
    }

    private fun startPollingForUnlock() {
        val userId = _state.value.userId ?: return
        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 15
            var unlocked = false

            while (attempts < maxAttempts && !unlocked) {
                delay(2000)

                try {
                    val result = getUserLanguagesUseCase(userId).filter { it !is Resource.Loading }.first()

                    if (result is Resource.Success) {
                        val activeLang = result.data?.find { it.status.name == "ACTIVE" }
                        val isIdInList = activeLang?.unlockedContent?.cities?.contains(contentId) == true ||
                                activeLang?.unlockedContent?.questPoints?.contains(contentId) == true ||
                                activeLang?.unlockedContent?.quests?.contains(contentId) == true

                        if (isIdInList) {
                            unlocked = true
                            _state.value = _state.value.copy(isUnlocked = true)
                            achievementMonitor.check()
                        }
                    }
                } catch (e: Exception) {
                }
                attempts++
            }

            if (!unlocked) {
                _state.value = _state.value.copy(
                    showSuccessPopup = false,
                    error = "Verificação pendente. Tente recarregar."
                )
                loadData()
            }
        }
    }

    fun dismissSuccessPopup() {
        _state.value = _state.value.copy(showSuccessPopup = false)
    }
}