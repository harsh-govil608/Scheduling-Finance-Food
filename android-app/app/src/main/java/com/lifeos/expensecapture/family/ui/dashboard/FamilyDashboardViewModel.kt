package com.lifeos.expensecapture.family.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.family.data.EventStreamRepository
import com.lifeos.expensecapture.family.data.FamilyLedgerRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.PresenceRepository
import com.lifeos.expensecapture.family.data.SharedCalendarRepository
import com.lifeos.expensecapture.family.data.SharedTaskRepository
import com.lifeos.expensecapture.family.model.FamilyEntity
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.MemberPresence
import com.lifeos.expensecapture.family.model.PermissionType
import com.lifeos.expensecapture.family.model.SharedCalendarEvent
import com.lifeos.expensecapture.family.model.SharedTask
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class FamilySpendSlice(val memberName: String, val amount: Double)

data class FamilyDashboardUiState(
    val family: FamilyEntity? = null,
    val members: List<FamilyMember> = emptyList(),
    val presence: List<MemberPresence> = emptyList(),
    val recentEvents: List<FamilyEvent> = emptyList(),
    val upcomingTasks: List<SharedTask> = emptyList(),
    val upcomingCalendarEvents: List<SharedCalendarEvent> = emptyList(),
    val currentMember: FamilyMember? = null,
    val insight: String? = null,
    /** Family Expense Tracker (2026-08 real user request) - today's SMS-auto-captured spend
     * across every member, synced by ParseIncomingSmsWorker. Debits only (a CREDIT like a
     * refund/salary isn't "spend"). */
    val totalFamilySpendToday: Double = 0.0,
    val spendByMemberToday: List<FamilySpendSlice> = emptyList(),
    /** Yesterday's same-metric total (2026-08, `ui3/` reference's "12% less than yesterday" line)
     * - null when there's no spend logged yesterday at all, so the UI can skip a "0% change" that
     * would actually mean "nothing to compare against." */
    val totalFamilySpendYesterday: Double? = null,
    val loading: Boolean = true
)

/** Groups the member/task/calendar/ledger reads into one snapshot so the top-level combine stays
 * a 3-arg overload - same pattern as HomeViewModel's FinanceSnapshot. */
private data class DashboardContentSnapshot(
    val members: List<FamilyMember>,
    val presence: List<MemberPresence>,
    val recentEvents: List<FamilyEvent>,
    val tasks: List<SharedTask>,
    val calendarEvents: List<SharedCalendarEvent>
)

/**
 * Family Dashboard (2026-08 Family module PRD: "member status, recent activity, reminders,
 * upcoming events, and AI insights"). `insight` is a deterministic heuristic over real data - no
 * different in kind from SpendingInsightEngine/ProductivityInsightEngine elsewhere in this app,
 * not a model call.
 */
class FamilyDashboardViewModel(
    familyId: String,
    private val currentUserId: String,
    familyRepository: FamilyRepository = FamilyRepository(),
    presenceRepository: PresenceRepository = PresenceRepository(),
    eventStreamRepository: EventStreamRepository = EventStreamRepository(),
    taskRepository: SharedTaskRepository = SharedTaskRepository(familyId = familyId),
    calendarRepository: SharedCalendarRepository = SharedCalendarRepository(familyId = familyId),
    ledgerRepository: FamilyLedgerRepository = FamilyLedgerRepository()
) : ViewModel() {

    private val content = combine(
        familyRepository.observeMembers(familyId),
        presenceRepository.observePresence(familyId),
        eventStreamRepository.observeRecentEvents(familyId),
        taskRepository.observeAll(),
        calendarRepository.observeAll()
    ) { members, presence, events, tasks, calendarEvents ->
        DashboardContentSnapshot(members, presence, events, tasks, calendarEvents)
    }

    // Computed once per ViewModel instance (i.e. per time the Dashboard is opened) rather than
    // ticking live at midnight - the Dashboard is a short-lived screen, not a background service,
    // so the day boundary going stale mid-session is an acceptable simplification here (unlike
    // Finance's Home, which fixed this exact class of bug for a screen people leave open for
    // hours - see TickerFlow's kdoc).
    private val todayStartMillis = LocalDate.now(ZoneId.systemDefault())
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val yesterdayStartMillis = todayStartMillis - 24L * 60 * 60 * 1000
    // One query from yesterday's start covers both cards (today's total + yesterday's comparison)
    // - split client-side by `date` rather than a second Firestore listener for the same collection.
    private val recentLedgerEntries = ledgerRepository.observeEntries(familyId, yesterdayStartMillis)

    val uiState: StateFlow<FamilyDashboardUiState> = combine(
        familyRepository.observeFamily(familyId),
        content,
        recentLedgerEntries
    ) { family, snapshot, ledgerEntries ->
        val currentMember = snapshot.members.firstOrNull { it.userId == currentUserId }
        val now = System.currentTimeMillis()
        val weekAhead = now + 7L * 24 * 60 * 60 * 1000

        val upcomingTasks = snapshot.tasks
            .filter { !it.completed && it.dueDate != null && it.dueDate in now..weekAhead }
            .sortedBy { it.dueDate }
        val upcomingCalendarEvents = snapshot.calendarEvents
            .filter { it.startAt in now..weekAhead }
            .sortedBy { it.startAt }

        val todaySpend = ledgerEntries.filter { it.direction == "DEBIT" && it.date >= todayStartMillis }
        val yesterdaySpend = ledgerEntries.filter {
            it.direction == "DEBIT" && it.date in yesterdayStartMillis until todayStartMillis
        }
        val spendByMember = todaySpend
            .groupBy { it.memberName }
            .mapValues { (_, entries) -> entries.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .map { FamilySpendSlice(it.key, it.value) }

        FamilyDashboardUiState(
            family = family,
            members = snapshot.members,
            presence = snapshot.presence,
            recentEvents = snapshot.recentEvents,
            upcomingTasks = upcomingTasks,
            upcomingCalendarEvents = upcomingCalendarEvents,
            currentMember = currentMember,
            insight = buildInsight(snapshot.members, upcomingTasks, snapshot.presence),
            totalFamilySpendToday = todaySpend.sumOf { it.amount },
            spendByMemberToday = spendByMember,
            totalFamilySpendYesterday = yesterdaySpend.sumOf { it.amount }.takeIf { it > 0.0 },
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyDashboardUiState())

    /** Plain heuristics, not a model call - see this class's own kdoc. Picks the single most
     * useful line rather than listing everything, same "one slot" discipline HomeViewModel's
     * AttentionItem arbitration already uses for Finance's Home. */
    private fun buildInsight(
        members: List<FamilyMember>,
        upcomingTasks: List<SharedTask>,
        presence: List<MemberPresence>
    ): String? {
        val overdueCount = upcomingTasks.count { it.dueDate != null && it.dueDate < System.currentTimeMillis() }
        if (overdueCount > 0) return "$overdueCount task${if (overdueCount == 1) "" else "s"} overdue - worth a check-in."
        if (upcomingTasks.isNotEmpty()) {
            return "${upcomingTasks.size} task${if (upcomingTasks.size == 1) "" else "s"} due in the next 7 days."
        }
        val staleMembers = presence.count {
            it.lastSeenAt in 1 until (System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000)
        }
        if (staleMembers > 0 && members.size > 1) {
            return "$staleMembers member${if (staleMembers == 1) "" else "s"} haven't opened the app in 3+ days."
        }
        return null
    }
}

fun visibleLocationCount(members: List<FamilyMember>, presence: List<MemberPresence>): Int {
    val visibleIds = members.filter { it.permissions.isVisible(PermissionType.LOCATION) }.map { it.userId }.toSet()
    return presence.count { it.userId in visibleIds && it.lastLocation != null }
}
