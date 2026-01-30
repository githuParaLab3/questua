package com.questua.app.presentation.navigation

sealed class Screen(val route: String) {
    object Initial : Screen("initial_screen")
    object Login : Screen("login_screen")
    object LanguageSelection : Screen("language_selection_screen")

    object Register : Screen("register_screen/{languageId}") {
        fun passId(languageId: String): String {
            return "register_screen/$languageId"
        }
    }

    object Home : Screen("home_screen")
    object LanguagesList : Screen("languages_list_screen")
    object WorldMap : Screen("world_map_screen")
    object Help : Screen("help_screen")
    object Feedback : Screen("feedback_screen/{reportType}") {
        fun passReportType(type: String): String {
            return "feedback_screen/$type"
        }
    }

    object CityDetail : Screen("city_detail_screen/{cityId}") {
        fun passId(cityId: String): String {
            return "city_detail_screen/$cityId"
        }
    }

    object AdminHome : Screen("admin_home_screen")
    object AdminUsers : Screen("admin_users_screen")
    object AdminFeedbackList : Screen("admin_feedback_list_screen")
    object AdminMonetization : Screen("admin_monetization_screen")

    object AdminMonetizationDetail : Screen("admin_monetization_detail/{productId}") {
        fun passId(productId: String): String {
            return "admin_monetization_detail/$productId"
        }
    }

    object AdminContentDetail : Screen("admin_content_detail/{contentType}") {
        fun passType(contentType: String): String {
            return "admin_content_detail/$contentType"
        }
    }

    object AdminLogs : Screen("admin_logs_screen")

    object AdminReportDetail : Screen("admin_report_detail/{reportId}") {
        fun passId(reportId: String): String {
            return "admin_report_detail/$reportId"
        }
    }

    object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        fun passId(userId: String): String {
            return "admin_user_detail/$userId"
        }
    }

    data object AdminLanguages : Screen("admin_languages")

    data object AdminCharacters : Screen("admin_characters")

    data object AdminAchievements : Screen("admin_achievements")

    data object AdminQuestPoints : Screen("admin_quest_points")

    data object AdminCities : Screen("admin_cities")

    data object AdminQuests : Screen("admin_quests")

    data object AdminDialogues : Screen("admin_dialogues")

    object AdminLogDetail : Screen("admin_log_detail/{logId}") {
        fun passId(logId: String): String {
            return "admin_log_detail/$logId"
        }
    }

    object AdminCityDetail : Screen("admin_city_detail/{cityId}") {
        fun passId(cityId: String) = "admin_city_detail/$cityId"
    }

    object AdminQuestDetail : Screen("admin_quest_detail/{questId}") {
        fun passId(id: String) = "admin_quest_detail/$id"
    }

    object AdminQuestPointDetail : Screen("admin_quest_point_detail/{pointId}") {
        fun passId(id: String) = "admin_quest_point_detail/$id"
    }

    object AdminDialogueDetail : Screen("admin_dialogue_detail/{dialogueId}") {
        fun passId(id: String) = "admin_dialogue_detail/$id"
    }
    object AdminCharacterDetail : Screen("admin_character_detail/{characterId}") {
        fun passId(id: String) = "admin_character_detail/$id"
    }
    object AdminAchievementDetail : Screen("admin_achievement_detail/{achievementId}") {
        fun passId(id: String) = "admin_achievement_detail/$id"
    }
    object AiGeneration : Screen("ai_generation")
}