package com.lifeos.expensecapture.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ConsentDao
import com.lifeos.expensecapture.data.db.dao.NotificationDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.dao.UnparsedMessageDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.notifications.NotificationCheckWorker
import com.lifeos.expensecapture.ui.onboarding.CONSENT_SMS
import com.lifeos.expensecapture.util.ConnectivityObserver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Finance Tracker (Home) PRD, Phase 3 Doc 17. The "needs attention" arbitration rule (Section
 * 8: "when two or more features' signals compete for the single slot") is a fixed precedence
 * here: an overdue bill (real financial consequence) beats an over-budget category
 * (informational) beats a pending review-queue count (lowest urgency).
 */
sealed class AttentionItem {
    data class OverdueBill(val payee: String, val amount: Double) : AttentionItem()
    data class OverBudget(val categoryName: String, val overspendAmount: Double) : AttentionItem()
    data class UnparsedMessages(val count: Int) : AttentionItem()
}

data class HomeUiState(
    val spentThisMonth: Double = 0.0,
    val attentionItem: AttentionItem? = null,
    val hasAnyData: Boolean = false,
    val unreadNotifications: Int = 0,
    val isOnline: Boolean = true,
    val smsPermissionRevoked: Boolean = false
)

/** Intermediate grouping to keep the combine() chain to 2-5 arg overloads instead of unsafe
 * array-casting across 7 heterogeneous flows. */
private data class FinanceSnapshot(
    val transactions: List<com.lifeos.expensecapture.data.db.entity.TransactionEntity>,
    val budgets: List<FinanceInsightsRepository.BudgetProgress>,
    val bills: List<FinanceInsightsRepository.BillWithComputedStatus>,
    val unparsedCount: Int
)

private data class StatusSnapshot(
    val unreadNotifications: Int,
    val isOnline: Boolean,
    val smsConsentedButRevoked: Boolean
)

class HomeViewModel(
    context: Context,
    transactionDao: TransactionDao,
    unparsedMessageDao: UnparsedMessageDao,
    notificationDao: NotificationDao,
    consentDao: ConsentDao,
    private val insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    init {
        viewModelScope.launch { insightsRepository.refreshRecurringDetection() }
        // Also runs the notification check on every Home open, not just the 6-hour schedule,
        // so testers see fresh results without waiting.
        NotificationCheckWorker.runOnce(context)
    }

    private val financeSnapshot = combine(
        transactionDao.observeAll(),
        insightsRepository.observeBudgetProgress(),
        insightsRepository.observeBills(),
        unparsedMessageDao.observeUnresolved()
    ) { transactions, budgets, bills, unparsed ->
        FinanceSnapshot(transactions, budgets, bills, unparsed.size)
    }

    private val statusSnapshot = combine(
        notificationDao.observeUnreadCount(),
        ConnectivityObserver.isOnlineFlow(context),
        consentDao.observeAll()
    ) { unreadCount, isOnline, consents ->
        val smsConsent = consents.firstOrNull { it.permissionType == CONSENT_SMS }
        val smsGrantedNow = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
        StatusSnapshot(
            unreadNotifications = unreadCount,
            isOnline = isOnline,
            smsConsentedButRevoked = smsConsent?.granted == true && !smsGrantedNow
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(financeSnapshot, statusSnapshot) { finance, status ->
        val zone = ZoneId.systemDefault()
        val monthStart = LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val spent = finance.transactions
            .filter { it.direction == TransactionDirection.DEBIT && it.date >= monthStart }
            .sumOf { it.amount }

        val overdueBill = finance.bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE }
        val overBudget = finance.budgets.firstOrNull { it.spentThisMonth > it.budget.monthlyLimit }

        val attentionItem = when {
            overdueBill != null -> AttentionItem.OverdueBill(overdueBill.bill.payeeDisplay, overdueBill.bill.typicalAmount)
            overBudget != null -> AttentionItem.OverBudget(
                overBudget.categoryName,
                overBudget.spentThisMonth - overBudget.budget.monthlyLimit
            )
            finance.unparsedCount > 0 -> AttentionItem.UnparsedMessages(finance.unparsedCount)
            else -> null
        }

        HomeUiState(
            spentThisMonth = spent,
            attentionItem = attentionItem,
            hasAnyData = finance.transactions.isNotEmpty(),
            unreadNotifications = status.unreadNotifications,
            isOnline = status.isOnline,
            smsPermissionRevoked = status.smsConsentedButRevoked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
