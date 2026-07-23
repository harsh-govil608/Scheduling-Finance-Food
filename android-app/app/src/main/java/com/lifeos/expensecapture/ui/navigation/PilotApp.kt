package com.lifeos.expensecapture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.bills.BillsScreen
import com.lifeos.expensecapture.ui.budget.BudgetScreen
import com.lifeos.expensecapture.ui.home.HomeScreen
import com.lifeos.expensecapture.ui.investments.InvestmentsScreen
import com.lifeos.expensecapture.ui.ledger.LedgerScreen
import com.lifeos.expensecapture.ui.nightsummary.NightSummaryScreen
import com.lifeos.expensecapture.ui.notifications.NotificationCenterScreen
import com.lifeos.expensecapture.ui.onboarding.PermissionScreen
import com.lifeos.expensecapture.ui.permissions.PermissionsScreen
import com.lifeos.expensecapture.ui.profile.ProfileScreen
import com.lifeos.expensecapture.ui.review.UnparsedReviewScreen
import com.lifeos.expensecapture.ui.rules.AutomationRulesScreen
import com.lifeos.expensecapture.ui.search.SearchScreen
import com.lifeos.expensecapture.ui.subscriptions.SubscriptionsScreen

/**
 * permission (Onboarding, Doc 40) -> home (Finance Tracker Home, Doc 17) -> every other
 * Finance Suite / cross-cutting screen. Route names double as Notification Center deep-link
 * targets (Doc 03) - keep them stable.
 */
@Composable
fun PilotApp(app: App) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "permission") {
        composable("permission") {
            PermissionScreen(
                onGranted = {
                    navController.navigate("home") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                app = app,
                onOpenLedger = { navController.navigate("ledger") },
                onOpenBudgets = { navController.navigate("budgets") },
                onOpenSubscriptions = { navController.navigate("subscriptions") },
                onOpenBills = { navController.navigate("bills") },
                onOpenNeedsReview = { navController.navigate("needs_review") },
                onOpenNotifications = { navController.navigate("notifications") },
                onOpenSearch = { navController.navigate("search") },
                onOpenInvestments = { navController.navigate("investments") },
                onOpenNightSummary = { navController.navigate("night_summary") },
                onOpenProfile = { navController.navigate("profile") },
                onOpenPermissionsReview = { navController.navigate("permissions") }
            )
        }
        composable("ledger") {
            LedgerScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("budgets") {
            BudgetScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("subscriptions") {
            SubscriptionsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("bills") {
            BillsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("needs_review") {
            UnparsedReviewScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("notifications") {
            NotificationCenterScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }
        composable("search") {
            SearchScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("investments") {
            InvestmentsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("night_summary") {
            NightSummaryScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onOpenPermissions = { navController.navigate("permissions") },
                onOpenAutomationRules = { navController.navigate("rules") },
                onDataDeleted = {
                    navController.navigate("permission") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("permissions") {
            PermissionsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("rules") {
            AutomationRulesScreen(app = app, onBack = { navController.popBackStack() })
        }
    }
}
