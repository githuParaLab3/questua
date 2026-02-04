package com.questua.app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.questua.app.presentation.admin.AdminGeneralManagementScreen
import com.questua.app.presentation.admin.content.ContentDetailScreen
import com.questua.app.presentation.admin.content.achievements.AdminAchievementDetailScreen
import com.questua.app.presentation.admin.content.achievements.AdminAchievementScreen
import com.questua.app.presentation.admin.content.ai.AiContentGenerationScreen
import com.questua.app.presentation.admin.content.characters.AdminCharacterDetailScreen
import com.questua.app.presentation.admin.content.characters.AdminCharacterScreen
import com.questua.app.presentation.admin.content.cities.AdminCityScreen
import com.questua.app.presentation.admin.content.dialogues.AdminDialogueScreen
import com.questua.app.presentation.admin.content.languages.AdminLanguageScreen
import com.questua.app.presentation.admin.content.questpoints.AdminQuestPointScreen
import com.questua.app.presentation.admin.content.quests.AdminQuestScreen
import com.questua.app.presentation.admin.feedback.AdminFeedbackScreen
import com.questua.app.presentation.admin.feedback.AdminReportDetailScreen
import com.questua.app.presentation.admin.logs.AdminLogDetailScreen
import com.questua.app.presentation.admin.logs.AiLogsScreen
import com.questua.app.presentation.admin.monetization.AdminMonetizationScreen
import com.questua.app.presentation.admin.monetization.AdminProductDetailScreen
import com.questua.app.presentation.admin.users.UserDetailScreen
import com.questua.app.presentation.admin.users.UserManagementScreen
import com.questua.app.presentation.admin.content.cities.AdminCityDetailScreen
import com.questua.app.presentation.admin.content.dialogues.AdminDialogueDetailScreen
import com.questua.app.presentation.admin.content.questpoints.AdminQuestPointDetailScreen
import com.questua.app.presentation.admin.content.quests.AdminQuestDetailScreen
import com.questua.app.presentation.admin.monetization.AdminTransactionDetailScreen

fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.AdminHome.route,
        route = "admin_route"
    ) {
        composable(route = Screen.AdminHome.route) {
            AdminGeneralManagementScreen(
                navController = navController,
                onNavigateToLogs = {
                    navController.navigate(Screen.AdminLogs.route)
                },
                onExitAdmin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AdminContentDetail.route,
            arguments = listOf(navArgument("contentType") { type = NavType.StringType })
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments?.getString("contentType") ?: ""
            ContentDetailScreen(
                navController = navController,
                contentType = contentType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AdminLogs.route) {
            AiLogsScreen(
                navController = navController
            )
        }

        composable(route = Screen.AdminUsers.route) {
            UserManagementScreen(navController = navController)
        }

        composable(route = Screen.AdminFeedbackList.route) {
            AdminFeedbackScreen(navController = navController)
        }

        composable(route = Screen.AdminMonetization.route) {
            AdminMonetizationScreen(navController = navController)
        }

        composable(
            route = Screen.AdminMonetizationDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            AdminProductDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            AdminReportDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserDetailScreen(navController = navController)
        }

        composable(route = Screen.AdminLanguages.route) {
            AdminLanguageScreen(navController = navController)
        }

        composable(route = Screen.AdminCharacters.route) {
            AdminCharacterScreen(navController = navController)
        }

        composable(route = Screen.AdminAchievements.route) {
            AdminAchievementScreen(navController = navController)
        }

        composable(route = Screen.AdminQuestPoints.route) {
            AdminQuestPointScreen(navController = navController)
        }

        composable(route = Screen.AdminCities.route) {
            AdminCityScreen(navController = navController)
        }

        composable(route = Screen.AdminQuests.route) {
            AdminQuestScreen(navController = navController)
        }

        composable(route = Screen.AdminDialogues.route) {
            AdminDialogueScreen(navController = navController)
        }
        composable(
            route = Screen.AdminLogDetail.route,
            arguments = listOf(navArgument("logId") { type = NavType.StringType })
        ) {
            AdminLogDetailScreen(navController = navController)
        }
        composable(
            route = Screen.AdminCityDetail.route,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) {
            AdminCityDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminQuestDetail.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            AdminQuestDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminQuestPointDetail.route,
            arguments = listOf(navArgument("pointId") { type = NavType.StringType })
        ) {
            AdminQuestPointDetailScreen(navController = navController)
        }
        composable(
            route = Screen.AdminDialogueDetail.route,
            arguments = listOf(navArgument("dialogueId") { type = NavType.StringType })
        ) {
            AdminDialogueDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AdminCharacterDetail.route,
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.StringType
                }
            )
        ) {
            AdminCharacterDetailScreen(navController = navController)
        }
        composable(
            route = Screen.AdminAchievementDetail.route,
            arguments = listOf(navArgument("achievementId") { type = NavType.StringType })
        ) {
            AdminAchievementDetailScreen(navController = navController)
        }
        composable(Screen.AiGeneration.route) {
            AiContentGenerationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.AiGeneration.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AdminTransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) {
            AdminTransactionDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}