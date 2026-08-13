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
import com.lifeos.expensecapture.family.ui.FamilyEntryScreen
import com.lifeos.expensecapture.family.ui.FamilyInviteScreen
import com.lifeos.expensecapture.family.ui.FamilyMembersScreen
import com.lifeos.expensecapture.family.ui.FamilyMoreScreen
import com.lifeos.expensecapture.family.ui.FamilyNotificationCenterScreen
import com.lifeos.expensecapture.family.ui.FamilyPillar
import com.lifeos.expensecapture.family.ui.modules.CalendarModuleScreen
import com.lifeos.expensecapture.family.ui.modules.DocumentsModuleScreen
import com.lifeos.expensecapture.family.ui.modules.EmergencyContactsModuleScreen
import com.lifeos.expensecapture.family.ui.modules.ExpensesModuleScreen
import com.lifeos.expensecapture.family.ui.modules.HealthModuleScreen
import com.lifeos.expensecapture.family.ui.modules.TasksModuleScreen
import com.lifeos.expensecapture.family.ui.sos.SosScreen
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import com.lifeos.expensecapture.splitpay.ui.SmartSplitNotificationWatcher
import com.lifeos.expensecapture.splitpay.ui.SmartSplitCreateScreen
import com.lifeos.expensecapture.splitpay.ui.SmartSplitDetailScreen
import com.lifeos.expensecapture.splitpay.ui.SmartSplitsScreen
import com.lifeos.expensecapture.splitpay.ui.SplitHistoryScreen
import com.lifeos.expensecapture.ui.analytics.AnalyticsScreen
import com.lifeos.expensecapture.ui.assistant.AssistantScreen
import com.lifeos.expensecapture.ui.backup.BackupRestoreScreen
import com.lifeos.expensecapture.ui.bills.BillsScreen
import com.lifeos.expensecapture.ui.budget.BudgetScreen
import com.lifeos.expensecapture.ui.categories.CategoriesScreen
import com.lifeos.expensecapture.ui.calculator.CalculatorScreen
import com.lifeos.expensecapture.ui.goals.GoalsScreen
import com.lifeos.expensecapture.ui.habits.HabitsScreen
import com.lifeos.expensecapture.ui.home.HomeScreen
import com.lifeos.expensecapture.data.db.entity.NoteType
import com.lifeos.expensecapture.importer.ImportStatementScreen
import com.lifeos.expensecapture.ui.paycycle.PayCycleScreen
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
import com.lifeos.expensecapture.ui.splitexpenses.AddSplitExpenseScreen
import com.lifeos.expensecapture.ui.splitexpenses.SplitExpenseDetailScreen
import com.lifeos.expensecapture.ui.splitexpenses.SplitExpensesScreen
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
            Pillar.ANALYTICS -> "analytics"
            Pillar.AI -> "assistant"
            Pillar.PROFILE -> "profile"
        }
        navController.navigate(target) { launchSingleTop = true }
    }

    // Family module's own bottom bar (2026-08, `ui3/` reference) - mirrors selectPillar's exact
    // navigate+launchSingleTop pattern, just with familyId threaded into the non-Home routes
    // since those are nav args rather than internally derived (see FamilyEntryScreen's kdoc).
    fun selectFamilyPillar(pillar: FamilyPillar, familyId: String) {
        val target = when (pillar) {
            FamilyPillar.HOME -> "family"
            FamilyPillar.EXPENSES -> "family_expenses/$familyId"
            FamilyPillar.TASKS -> "family_tasks/$familyId"
            FamilyPillar.CALENDAR -> "family_calendar/$familyId"
            FamilyPillar.MORE -> "family_more/$familyId"
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

    // App-level (not per-screen) so a Smart Split notification fires no matter what screen the
    // user is on - see SmartSplitNotificationWatcher's kdoc for what triggers it and its limits.
    SmartSplitNotificationWatcher(app)

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
                onOpenSplitExpenses = { navController.navigate("split_expenses") },
                onOpenImportStatement = { navController.navigate("import_statement") },
                onOpenPayCycle = { navController.navigate("pay_cycle") },
                onOpenNotifications = { navController.navigate("notifications") },
                onOpenSearch = { navController.navigate("search") },
                onOpenInvestments = { navController.navigate("investments") },
                onOpenNightSummary = { navController.navigate("night_summary") },
                onOpenProfile = { navController.navigate("profile") },
                onOpenPermissionsReview = { navController.navigate("permissions") },
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
                onOpenFamily = { navController.navigate("family") },
                onSelectPillar = { pillar -> selectPillar(pillar) }
            )
        }
        composable("analytics") {
            AnalyticsScreen(
                app = app,
                onSelectPillar = { pillar -> selectPillar(pillar) },
                onOpenProfile = { navController.navigate("profile") }
            )
        }
        composable("assistant") {
            AssistantScreen(app = app, onSelectPillar = { pillar -> selectPillar(pillar) })
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
        composable("import_statement") {
            ImportStatementScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("pay_cycle") {
            PayCycleScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("split_expenses") {
            SplitExpensesScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onAddExpense = { navController.navigate("split_expenses/add") },
                onOpenDetail = { expenseId -> navController.navigate("split_expenses/$expenseId") },
                onOpenSmartSplit = { navController.navigate("smart_splits") }
            )
        }
        composable("smart_splits") {
            SmartSplitsScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.navigate("smart_split_create") },
                onOpenSplit = { splitId -> navController.navigate("smart_split_detail/$splitId") },
                onOpenHistory = { navController.navigate("smart_split_history") }
            )
        }
        composable("smart_split_history") {
            SplitHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable("smart_split_create") {
            SmartSplitCreateScreen(
                onBack = { navController.popBackStack() },
                onCreated = { splitId ->
                    navController.navigate("smart_split_detail/$splitId") {
                        popUpTo("smart_splits")
                    }
                }
            )
        }
        composable(
            route = "smart_split_detail/{splitId}",
            arguments = listOf(navArgument("splitId") { type = NavType.StringType })
        ) { backStackEntry ->
            SmartSplitDetailScreen(
                splitId = backStackEntry.arguments?.getString("splitId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable("split_expenses/add") {
            AddSplitExpenseScreen(
                app = app,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "split_expenses/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
            SplitExpenseDetailScreen(app = app, expenseId = expenseId, onBack = { navController.popBackStack() })
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
        composable("categories") {
            CategoriesScreen(app = app, onBack = { navController.popBackStack() })
        }
        composable("calculator") {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                app = app,
                onOpenPermissions = { navController.navigate("permissions") },
                onOpenAutomationRules = { navController.navigate("rules") },
                onOpenCategories = { navController.navigate("categories") },
                onOpenCalculator = { navController.navigate("calculator") },
                onOpenDiagnostics = { navController.navigate("diagnostics") },
                onOpenBackupRestore = { navController.navigate("backup_restore") },
                onOpenNotifications = { navController.navigate("notifications") },
                onOpenFamily = { navController.navigate("family") },
                onOpenPremium = { navController.navigate("premium") },
                onSelectPillar = { pillar -> selectPillar(pillar) },
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
        composable("premium") {
            com.lifeos.expensecapture.ui.paywall.PaywallScreen(app = app, onBack = { navController.popBackStack() })
        }

        // Family module (2026-08) - a cross-device, Firebase-backed module distinct from every
        // other route above (all local-only Room data, see AppDatabase's kdoc). Reached from
        // Profile's "Family" row rather than a 6th bottom-nav tab, keeping the 5-tab pillar
        // structure intact - see FamilyEntryScreen's kdoc. familyId travels as a nav arg into
        // every child route below "family" rather than each screen re-deriving "current family."
        composable("family") {
            FamilyEntryScreen(
                onOpenTasks = { familyId -> navController.navigate("family_tasks/$familyId") },
                onOpenCalendar = { familyId -> navController.navigate("family_calendar/$familyId") },
                onOpenExpenses = { familyId -> navController.navigate("family_expenses/$familyId") },
                onOpenInvite = { familyId -> navController.navigate("family_invite/$familyId") },
                onOpenSos = { familyId -> navController.navigate("family_sos/$familyId") },
                onOpenNotifications = { familyId -> navController.navigate("family_notifications/$familyId") },
                onBackToFinance = {
                    navController.navigate("home") {
                        popUpTo("family") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSelectPillar = { pillar, familyId -> selectFamilyPillar(pillar, familyId) }
            )
        }
        composable(
            route = "family_tasks/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            TasksModuleScreen(
                familyId = familyId,
                onBack = { navController.popBackStack() },
                onSelectPillar = { pillar -> selectFamilyPillar(pillar, familyId) }
            )
        }
        composable(
            route = "family_calendar/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            CalendarModuleScreen(
                familyId = familyId,
                onBack = { navController.popBackStack() },
                onSelectPillar = { pillar -> selectFamilyPillar(pillar, familyId) }
            )
        }
        composable(
            route = "family_expenses/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            ExpensesModuleScreen(
                familyId = familyId,
                onBack = { navController.popBackStack() },
                onSelectPillar = { pillar -> selectFamilyPillar(pillar, familyId) }
            )
        }
        composable(
            route = "family_more/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            FamilyMoreScreen(
                onBack = { navController.popBackStack() },
                onOpenSos = { navController.navigate("family_sos/$familyId") },
                onOpenMembers = { navController.navigate("family_members/$familyId") },
                onOpenInvite = { navController.navigate("family_invite/$familyId") },
                onOpenNotifications = { navController.navigate("family_notifications/$familyId") },
                onOpenDocuments = { navController.navigate("family_documents/$familyId") },
                onOpenHealth = { navController.navigate("family_health/$familyId") },
                onOpenEmergencyContacts = { navController.navigate("family_emergency_contacts/$familyId") },
                onSelectPillar = { pillar -> selectFamilyPillar(pillar, familyId) }
            )
        }
        composable(
            route = "family_documents/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            DocumentsModuleScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_health/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            HealthModuleScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_emergency_contacts/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            EmergencyContactsModuleScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_members/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            FamilyMembersScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_invite/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            FamilyInviteScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_sos/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val familyId = backStackEntry.arguments?.getString("familyId") ?: ""
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            SosScreen(
                familyId = familyId,
                userId = currentUser?.uid ?: "",
                userName = currentUser?.displayName ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "family_notifications/{familyId}",
            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
        ) { backStackEntry ->
            FamilyNotificationCenterScreen(
                familyId = backStackEntry.arguments?.getString("familyId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
