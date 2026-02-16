package com.questua.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.questua.app.presentation.admin.feedback.AdminReportDetailScreen
import com.questua.app.presentation.auth.LoginScreen
import com.questua.app.presentation.auth.RegisterScreen
import com.questua.app.presentation.common.InitialScreen
import com.questua.app.presentation.exploration.city.CityDetailScreen
import com.questua.app.presentation.exploration.questpoint.QuestPointScreen
import com.questua.app.presentation.game.DialogueScreen
import com.questua.app.presentation.game.QuestIntroScreen
import com.questua.app.presentation.game.QuestResultScreen
import com.questua.app.presentation.languages.LanguagesListScreen
import com.questua.app.presentation.main.MainScreen
import com.questua.app.presentation.monetization.PaymentScreen
import com.questua.app.presentation.monetization.UnlockPreviewScreen
import com.questua.app.presentation.monetization.UnlockPreviewViewModel
import com.questua.app.presentation.onboarding.LanguageSelectionScreen
import com.questua.app.presentation.profile.FeedbackScreen
import com.questua.app.presentation.profile.HelpScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Initial.route) {
            InitialScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.LanguageSelection.route) }
            )
        }

        composable(route = Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = { languageId ->
                    navController.navigate(Screen.Register.passId(languageId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Register.route,
            arguments = listOf(navArgument("languageId") { type = NavType.StringType })
        ) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Initial.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.LanguageSelection.route) }
            )
        }

        composable(route = Screen.Home.route) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToLanguages = {
                    navController.navigate(Screen.LanguagesList.route)
                },
                onNavigateToCity = { cityId ->
                    navController.navigate(Screen.CityDetail.passId(cityId))
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminHome.route)
                },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToFeedback = { type ->
                    navController.navigate(Screen.Feedback.passReportType(type.name))
                },
                navController = navController
            )
        }

        composable(route = Screen.LanguagesList.route) {
            LanguagesListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Help.route) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReport = { type ->
                    navController.navigate(Screen.Feedback.passReportType(type.name))
                }
            )
        }

        composable(
            route = Screen.Feedback.route,
            arguments = listOf(navArgument("reportType") { type = NavType.StringType })
        ) {
            val previousBackStackEntry = navController.previousBackStackEntry

            FeedbackScreen(
                onNavigateBack = { navController.popBackStack() },
                onReportSent = { successMessage ->
                    previousBackStackEntry?.savedStateHandle?.set("feedback_message", successMessage)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CityDetail.route,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) {
            CityDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestPoint = { pointId ->
                    navController.navigate(Screen.QuestPoint.passId(pointId))
                }
            )
        }

        composable(
            route = Screen.QuestPoint.route,
            arguments = listOf(navArgument("pointId") { type = NavType.StringType })
        ) {
            QuestPointScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuestClick = { questId ->
                    navController.navigate(Screen.QuestIntro.passId(questId))
                },
                onNavigateToUnlock = { contentId, contentType ->
                    navController.navigate(Screen.UnlockPreview.passArgs(contentId, contentType))
                }
            )
        }

        adminNavGraph(navController)

        composable(
            route = Screen.AdminReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            AdminReportDetailScreen(navController = navController)
        }

        composable(
            route = Screen.QuestIntro.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            QuestIntroScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartGameplay = { questId ->
                    navController.navigate(Screen.Dialogue.passId(questId))
                }
            )
        }

        // =====================================================================
        // CORREÇÃO E NOVO FLUXO DE PAGAMENTO
        // =====================================================================

        composable(
            route = Screen.UnlockPreview.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.StringType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Recupera o ViewModel aqui para poder chamar o refresh()
            val viewModel: UnlockPreviewViewModel = hiltViewModel()

            // Observa se voltamos do pagamento com sucesso
            val paymentSuccess = backStackEntry.savedStateHandle.get<Boolean>("payment_success")
            LaunchedEffect(paymentSuccess) {
                if (paymentSuccess == true) {
                    viewModel.refresh()
                    // Limpa o estado para não rodar novamente à toa
                    backStackEntry.savedStateHandle.remove<Boolean>("payment_success")
                }
            }

            UnlockPreviewScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                // Atualizado para receber productId e userId e navegar para Payment
                onNavigateToPayment = { productId, userId ->
                    navController.navigate(Screen.Payment.createRoute(productId, userId))
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            PaymentScreen(
                productId = productId,
                userId = userId,
                onPaymentSuccess = {
                    // Avisa a tela anterior (UnlockPreview) que o pagamento rolou
                    navController.previousBackStackEntry?.savedStateHandle?.set("payment_success", true)
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // =====================================================================

        composable(
            route = Screen.Dialogue.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            DialogueScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuestCompleted = { questId, xp, correct, total ->
                    navController.navigate(
                        Screen.QuestResult.createRoute(questId, xp, correct, total)
                    ) {
                        popUpTo(Screen.QuestPoint.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.QuestResult.route,
            arguments = listOf(
                navArgument("questId") { type = NavType.StringType },
                navArgument("xpEarned") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType }
            )
        ) {
            QuestResultScreen(
                onNavigateToQuest = { nextQuestId ->
                    navController.navigate(Screen.QuestIntro.passId(nextQuestId)) {
                        popUpTo(Screen.QuestResult.route) { inclusive = true }
                    }
                },
                onNavigateBackToPoint = { _ ->
                    navController.popBackStack()
                }
            )
        }
    }
}