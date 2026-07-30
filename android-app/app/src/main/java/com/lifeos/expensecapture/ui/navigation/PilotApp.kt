package com.lifeos.expensecapture.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import com.lifeos.expensecapture.ui.assistant.AssistantScreen
import com.lifeos.expensecapture.ui.backup.BackupRestoreScreen
import com.lifeos.expensecapture.ui.bills.BillsScreen
import com.lifeos.expensecapture.ui.budget.BudgetScreen
import com.lifeos.expensecapture.ui.goals.GoalsScreen
import com.lifeos.expensecapture.ui.habits.HabitsScreen
import com.lifeos.expensecapture.ui.home.HomeScreen
import com.lifeos.expensecapture.data.db.entity.NoteType
import com.lifeos.expensecapture.ui.investments.InvestmentsScreen
import com.lifeos.expensecapture.ui.ledger.LedgerScreen
import com.lifeos.expensecapture.ui.nightsummary.NightSummaryScreen
import com.lifeos.expensecapture.ui.notes.NotesScreen
import com.lifeos.expensecapture.ui.notifications.NotificationCenterScreen
import com.lifeos.expensecapture.ui.onboarding.PermissionScreen
import com.lifeos.expensecapture.ui.permissions.PermissionsScreen
import com.lifeos.expensecapture.ui.productivity.ProductivityHomeScreen
import com.lifeos.expensecapture.ui.diagnostics.DiagnosticsScreen
import com.lifeos.expensecapture.ui.profile.ProfileScreen
import com.lifeos.expensecapture.ui.projects.ProjectDetailScreen
import com.lifeos.expensecapture.ui.projects.ProjectsScreen
import com.lifeos.expensecapture.ui.review.UnparsedReviewScreen
import com.lifeos.expensecapture.ui.rules.AutomationRulesScreen
import com.lifeos.expensecapture.ui.search.SearchScreen
import com.lifeos.expensecapture.ui.shopping.ShoppingScreen
import com.lifeos.expensecapture.ui.subscriptions.SubscriptionsScreen
import com.lifeos.expensecapture.ui.tasks.TaskListScreen
import com.lifeos.expensecapture.ui.timeline.ContextTimelineScreen
import com.lifeos.expensecapture.ui.weeklyreview.ReviewScreen
import kotlinx.coroutines.launch

/**
 * permission (Onboarding, Doc 40) -> home (Finance Tracker Home, Doc 17) -> every other
 * Finance Suite / cross-cutting screen, PLUS a second pillar as of today: productivity_home
 * (Home: Task Management Doc 10 + Habits Doc 13) -> tasks / habits. The two pillar roots
 * ("home" and "productivity_home") are switched between via PillarBottomBar rather than a
 * nested NavHost - a deliberate simplification (see PillarBottomBar's kdoc) since this is a
 * 2-tab pilot, not a case needing full back-stack state preservation per tab. Route names double
 * as Notification Center deep-link targets (Doc 03) - keep them stable.
 */
@Composable
fun PilotApp(app: App) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    fun selectPillar(pillar: Pillar) {
        val target = when (pillar) {
            Pillar.FINANCE -> "home"
            Pillar.HOME -> "productivity_home"
        }
        navController.navigate(target) { launchSingleTop = true }
    }

    // SmsHistoryScanner's catch-up scan otherwise only resumes on a genuine cold start
    // (PermissionScreen's own LaunchedEffect) or the 6-hour periodic worker - simply switching
    // away from the app and back (without killing the process) recomposes nothing and would
    // silently NOT retry. This makes every resume a retry too, closing that gap: a user working
    // around a stuck scan by "closing and reopening" the app should reliably make progress
    // regardless of which of those two things they actually did.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasSmsPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasSmsPermission) {
                    coroutineScope.launch { SmsHistoryScanner.scanIfNeeded(context) }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                onOpenPermissionsReview = { navController.navigate("permissions") },
                onOpenAssistant = { navController.navigate("assistant") },
                onSelectPillar = { pillar -> selectPillar(pillar) }
            )
        }
        composable("productivity_home") {
            ProductivityHomeScreen(
                app = app,
                onOpenTasks = { navController.navigate("tasks") },
                onOpenHabits = { navController.navigate("habits") },
                onOpenGoals = { navController.navigate("goals") },
                onOpenProjects = { navController.navigate("projects") },
                onOpenReview = { navController.navigate("review") },
                onOpenNotes = { navController.navigate("notes") },
                onOpenJournal = { navController.navigate("journal") },
                onOpenShopping = { navController.navigate("shopping") },
                onOpenTimeline = { navController.navigate("timeline") },
                onOpenLedger = { navController.navigate("ledger") },
                onOpenProfile = { navController.navigate("profile") },
                onOpenAssistant = { navController.navigate("assistant") },
                onSelectPillar = { pillar -> selectPillar(pillar) }
            )
        }
        composable("assistant") {
            AssistantScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("notes") {
            NotesScreen(app = app, type = NoteType.NOTE, screenTitle = "Notes", onBack = { navController.popBackStack() })
        }
        composable("journal") {
            NotesScreen(app = app, type = NoteType.JOURNAL, screenTitle = "Journal", onBack = { navController.popBackStack() })
        }
        composable("shopping") {
            ShoppingScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("timeline") {
            ContextTimelineScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("tasks") {
            TaskListScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("habits") {
            HabitsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("goals") {
            GoalsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("projects") {
            ProjectsScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onOpenProject = { projectId ->
                    navController.navigate("project_detail/$projectId")
                }
            )
        }
        composable(
            route = "project_detail/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ProjectDetailScreen(
                app = app,
                projectId = projectId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("review") {
            ReviewScreen(app = app, onBack = { navController.popBackStack() })
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
                onOpenDiagnostics = { navController.navigate("diagnostics") },
                onOpenBackupRestore = { navController.navigate("backup_restore") },
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
        composable("diagnostics") {
            DiagnosticsScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("backup_restore") {
            BackupRestoreScreen(app = app, onBack = { navController.popBackStack() })
        }
    }
}
