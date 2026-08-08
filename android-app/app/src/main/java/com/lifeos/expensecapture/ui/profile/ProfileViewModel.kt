package com.lifeos.expensecapture.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.BudgetEntity
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.finance.FinancialHealthScore
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import com.lifeos.expensecapture.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/** Keeps the combine() below to 4-arg overloads instead of an untyped vararg across 7
 * heterogeneous flows - same pattern as HomeViewModel's FinanceSnapshot/StatusSnapshot. */
private data class ProfileStatsSnapshot(
    val transactions: List<TransactionEntity>,
    val goals: List<GoalEntity>,
    val habits: List<HabitEntity>,
    val budgets: List<BudgetEntity>
)

data class ProfileUiState(
    val displayName: String = "",
    val capturePaused: Boolean = false,
    val profilePhotoPath: String? = null,
    val appVersionName: String = "",
    val transactionCount: Int = 0,
    val goalCount: Int = 0,
    val habitCount: Int = 0,
    /** Same deterministic formula as Analytics' card - see FinancialHealthScore's kdoc. */
    val healthScore: Int = 50
)

/**
 * Account & Profile Management PRD, Phase 3 Doc 44, scoped to what's achievable with no
 * backend/auth: no account credentials exist to manage. What DOES apply: a display name, the
 * centralized pause control the User Control Model calls for, and "delete my account" -
 * reinterpreted honestly for a local-only app as "delete all locally stored data," since
 * there's no server-side account to delete in the first place.
 *
 * Stats row + grouped sections (2026-08 reference mockups, `ui2/` folder) - Transactions/Goals/
 * Habits are real counts, Score is the same real FinancialHealthScore Analytics shows, not a
 * fabricated "Premium Member"/gamification number the mockup's own placeholder data implied.
 */
class ProfileViewModel(private val context: Context, private val database: AppDatabase) : ViewModel() {

    private val _capturePaused = MutableStateFlow(Prefs.isCapturePaused(context))
    private val _profilePhotoPath = MutableStateFlow(Prefs.getProfilePhotoPath(context))
    private val _displayName = MutableStateFlow(Prefs.getDisplayName(context))

    private val statsSnapshot = combine(
        database.transactionDao().observeAll(),
        database.goalDao().observeAll(),
        database.habitDao().observeAll(),
        database.budgetDao().observeAll()
    ) { transactions, goals, habits, budgets ->
        ProfileStatsSnapshot(transactions, goals, habits, budgets)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        statsSnapshot,
        _capturePaused,
        _profilePhotoPath,
        _displayName
    ) { stats, capturePaused, profilePhotoPath, displayName ->
        val (transactions, goals, habits, budgets) = stats

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val prevMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val thisMonthTxns = transactions.filter { it.date >= monthStart }
        val spentThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        val incomeThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
        val prevMonthSpent = transactions
            .filter { it.date in prevMonthStart until monthStart && it.direction == TransactionDirection.DEBIT }
            .sumOf { it.amount }
        val budgetRatios = budgets.mapNotNull { budget ->
            if (budget.monthlyLimit <= 0) return@mapNotNull null
            val relevant = thisMonthTxns.filter {
                it.direction == TransactionDirection.DEBIT &&
                    (budget.categoryId == null || it.categoryId == budget.categoryId)
            }
            relevant.sumOf { it.amount } / budget.monthlyLimit
        }
        val healthScore = FinancialHealthScore.compute(
            FinancialHealthScore.Inputs(
                incomeThisMonth = incomeThisMonth,
                spentThisMonth = spentThisMonth,
                prevMonthSpent = prevMonthSpent,
                budgetUtilizationRatios = budgetRatios
            )
        )

        ProfileUiState(
            displayName = displayName,
            capturePaused = capturePaused,
            profilePhotoPath = profilePhotoPath,
            appVersionName = AppLogger.appVersionName(context),
            transactionCount = transactions.size,
            goalCount = goals.size,
            habitCount = habits.size,
            healthScore = healthScore
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun setDisplayName(name: String) {
        Prefs.setDisplayName(context, name)
        _displayName.value = name
    }

    /** Copies the picker's selected image into app-internal storage rather than keeping its
     * content:// Uri directly - the system photo picker's read grant isn't guaranteed to survive
     * process death/reboot, so the Uri alone isn't a durable reference. */
    fun setProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                val target = File(context.filesDir, "profile_photo.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                target.absolutePath
            }
            Prefs.setProfilePhotoPath(context, path)
            _profilePhotoPath.value = path
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _profilePhotoPath.value?.let { File(it).delete() }
            }
            Prefs.setProfilePhotoPath(context, null)
            _profilePhotoPath.value = null
        }
    }

    fun setCapturePaused(paused: Boolean) {
        Prefs.setCapturePaused(context, paused)
        _capturePaused.value = paused
    }

    /**
     * "Clear all my data and log out" (renamed from "Delete All My Data" - real founder report,
     * 2026-08: this appeared not to work). It did wipe the Room tables, but nothing ever came
     * back afterward, which reads as broken: SmsHistoryScanner's `last_scanned_date` watermark
     * lives in its own SharedPreferences file, not a Room table, so `clearAllTables()` never
     * touched it - the very next scan (ProfileScreen already navigates back to onboarding, which
     * re-runs one) believed every message up to that watermark was already handled and skipped
     * all of it, silently leaving the ledger empty forever. This is the exact same class of bug
     * AppDatabase's onDestructiveMigration callback already exists to prevent for a schema-bump
     * wipe (see SmsHistoryScanner.resetScanFlag's kdoc) - this manual path just never called it.
     *
     * Also now signs out of the Family module's Firebase account (a real "log out", not just a
     * local data wipe - the Family SDK session otherwise survives this) and clears the local
     * display name/photo, so this is genuinely a fresh start, not a fresh start with old identity
     * bits still attached.
     */
    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            database.clearAllTables()
            database.categoryDao().insertAll(DefaultCategories.asEntities())
            SmsHistoryScanner.resetScanFlag(context)
            Prefs.setCapturePaused(context, false)
            Prefs.setDisplayName(context, "")
            Prefs.getProfilePhotoPath(context)?.let { java.io.File(it).delete() }
            Prefs.setProfilePhotoPath(context, null)
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                AppLogger.e("ProfileViewModel", "sign-out failed during clear-all-data", e)
            }
            onDone()
        }
    }
}
