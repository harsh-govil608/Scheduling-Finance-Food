package com.lifeos.expensecapture.assistant

import android.content.Context
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.finance.FinanceQaEngine
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Executes a CommandIntent by calling the exact same repository/DAO methods every existing
 * Add/Edit UI already calls - see CommandIntent.kt's kdoc for why this is a separate layer from
 * the interpreter. Returns a plain-language confirmation (or a clear explanation of what's
 * missing) rather than throwing, so the chat UI always has something to show the user.
 */
class CommandExecutor(private val db: AppDatabase, private val context: Context) {

    suspend fun execute(intent: CommandIntent, history: List<ConversationTurn> = emptyList()): String = when (intent) {
        is CommandIntent.AddTransaction -> addTransaction(intent)
        is CommandIntent.AddTask -> addTask(intent)
        is CommandIntent.AddHabit -> addHabit(intent)
        is CommandIntent.AddShoppingItem -> addShoppingItem(intent)
        is CommandIntent.SetBudget -> setBudget(intent)
        is CommandIntent.CompleteTask -> completeTask(intent)
        is CommandIntent.DeleteTask -> deleteTask(intent)
        is CommandIntent.CompleteHabit -> completeHabit(intent)
        is CommandIntent.CheckShoppingItem -> checkShoppingItem(intent)
        is CommandIntent.ConfirmBill -> confirmBill(intent)
        is CommandIntent.DismissBill -> dismissBill(intent)
        is CommandIntent.ConfirmSubscription -> confirmSubscription(intent)
        is CommandIntent.DismissSubscription -> dismissSubscription(intent)
        is CommandIntent.RecategorizeTransaction -> recategorizeTransaction(intent)
        is CommandIntent.Unrecognized -> unrecognized(intent, history)
    }

    private suspend fun addTransaction(intent: CommandIntent.AddTransaction): String {
        val categories = db.categoryDao().observeAll().first()
        val categoryId = intent.categoryHint
            ?.let { hint -> categories.firstOrNull { it.name.contains(hint, ignoreCase = true) }?.id }
            ?: db.categoryDao().getUncategorized()?.id
            ?: return "Couldn't log that - the Uncategorized category is missing, which shouldn't happen."

        val repository = TransactionRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            merchantRuleDao = db.merchantRuleDao(),
            correctionDao = db.correctionDao()
        )
        repository.addManualTransaction(
            amount = intent.amount,
            direction = intent.direction,
            merchant = intent.merchant,
            categoryId = categoryId,
            date = System.currentTimeMillis()
        )
        val verb = if (intent.direction == TransactionDirection.DEBIT) "spent on" else "received from"
        return "Logged ₹${"%.2f".format(intent.amount)} $verb ${intent.merchant}."
    }

    private suspend fun addTask(intent: CommandIntent.AddTask): String {
        db.taskDao().insert(TaskEntity(title = intent.title, dueDate = intent.dueDate))
        return "Task added: ${intent.title}."
    }

    private suspend fun addHabit(intent: CommandIntent.AddHabit): String {
        db.habitDao().insert(HabitEntity(name = intent.name))
        return "Habit added: ${intent.name}."
    }

    private suspend fun addShoppingItem(intent: CommandIntent.AddShoppingItem): String {
        db.shoppingItemDao().insert(ShoppingItemEntity(name = intent.name, quantity = intent.quantity))
        return "Added \"${intent.name}\" to your shopping list."
    }

    private suspend fun setBudget(intent: CommandIntent.SetBudget): String {
        val categories = db.categoryDao().observeAll().first()
        val categoryId = intent.categoryHint?.let { hint ->
            categories.firstOrNull { it.name.contains(hint, ignoreCase = true) }?.id
        }
        if (intent.categoryHint != null && categoryId == null) {
            return "I couldn't find a category matching \"${intent.categoryHint}\" - your categories are: " +
                categories.joinToString(", ") { it.name } + "."
        }
        financeInsights().setBudget(categoryId, intent.monthlyLimit)
        val label = categoryId?.let { id -> categories.firstOrNull { it.id == id }?.name } ?: "Overall"
        return "Set $label budget to ₹${"%.2f".format(intent.monthlyLimit)}."
    }

    private suspend fun completeTask(intent: CommandIntent.CompleteTask): String {
        val match = db.taskDao().observeAll().first()
            .firstOrNull { !it.completed && it.title.contains(intent.titleMatch, ignoreCase = true) }
            ?: return "Couldn't find an open task matching \"${intent.titleMatch}\"."
        db.taskDao().update(match.copy(completed = true, completedAt = System.currentTimeMillis()))
        return "Marked \"${match.title}\" done."
    }

    private suspend fun deleteTask(intent: CommandIntent.DeleteTask): String {
        val match = db.taskDao().observeAll().first()
            .firstOrNull { it.title.contains(intent.titleMatch, ignoreCase = true) }
            ?: return "Couldn't find a task matching \"${intent.titleMatch}\"."
        db.taskDao().delete(match)
        return "Deleted task \"${match.title}\"."
    }

    private suspend fun completeHabit(intent: CommandIntent.CompleteHabit): String {
        val match = db.habitDao().observeAll().first()
            .firstOrNull { it.name.contains(intent.nameMatch, ignoreCase = true) }
            ?: return "Couldn't find a habit matching \"${intent.nameMatch}\"."
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        db.habitCompletionDao().insert(HabitCompletionEntity(habitId = match.id, dateEpochDay = today))
        return "Marked \"${match.name}\" done for today."
    }

    private suspend fun checkShoppingItem(intent: CommandIntent.CheckShoppingItem): String {
        val match = db.shoppingItemDao().observeAll().first()
            .firstOrNull { !it.checked && it.name.contains(intent.nameMatch, ignoreCase = true) }
            ?: return "Couldn't find an unchecked shopping item matching \"${intent.nameMatch}\"."
        db.shoppingItemDao().update(match.copy(checked = true, checkedAt = System.currentTimeMillis()))
        return "Checked off \"${match.name}\"."
    }

    private suspend fun confirmBill(intent: CommandIntent.ConfirmBill): String {
        val match = db.billDao().observeAll().first()
            .firstOrNull { it.payeeDisplay.contains(intent.payeeMatch, ignoreCase = true) }
            ?: return "Couldn't find a bill matching \"${intent.payeeMatch}\"."
        financeInsights().confirmBill(match)
        return "Now tracking \"${match.payeeDisplay}\" as a bill."
    }

    private suspend fun dismissBill(intent: CommandIntent.DismissBill): String {
        val match = db.billDao().observeAll().first()
            .firstOrNull { it.payeeDisplay.contains(intent.payeeMatch, ignoreCase = true) }
            ?: return "Couldn't find a bill matching \"${intent.payeeMatch}\"."
        financeInsights().dismissBill(match)
        return "\"${match.payeeDisplay}\" won't be tracked as a bill."
    }

    private suspend fun confirmSubscription(intent: CommandIntent.ConfirmSubscription): String {
        val match = db.subscriptionDao().observeAll().first()
            .firstOrNull { it.merchantDisplay.contains(intent.merchantMatch, ignoreCase = true) }
            ?: return "Couldn't find a subscription matching \"${intent.merchantMatch}\"."
        financeInsights().confirmSubscription(match)
        return "Now tracking \"${match.merchantDisplay}\" as a subscription."
    }

    private suspend fun dismissSubscription(intent: CommandIntent.DismissSubscription): String {
        val match = db.subscriptionDao().observeAll().first()
            .firstOrNull { it.merchantDisplay.contains(intent.merchantMatch, ignoreCase = true) }
            ?: return "Couldn't find a subscription matching \"${intent.merchantMatch}\"."
        financeInsights().dismissSubscription(match)
        return "\"${match.merchantDisplay}\" won't be tracked as a subscription."
    }

    private suspend fun recategorizeTransaction(intent: CommandIntent.RecategorizeTransaction): String {
        val categories = db.categoryDao().observeAll().first()
        val category = categories.firstOrNull { it.name.contains(intent.categoryName, ignoreCase = true) }
            ?: return "Couldn't find a category matching \"${intent.categoryName}\" - your categories are: " +
                categories.joinToString(", ") { it.name } + "."
        val match = db.transactionDao().observeAll().first()
            .filter { it.merchantRaw.contains(intent.merchantMatch, ignoreCase = true) }
            .maxByOrNull { it.date }
            ?: return "Couldn't find a transaction matching \"${intent.merchantMatch}\"."
        val repository = TransactionRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            merchantRuleDao = db.merchantRuleDao(),
            correctionDao = db.correctionDao()
        )
        repository.recategorize(match, category.id)
        return "Recategorized \"${match.merchantRaw}\" as ${category.name}."
    }

    private fun financeInsights() = FinanceInsightsRepository(
        transactionDao = db.transactionDao(),
        categoryDao = db.categoryDao(),
        budgetDao = db.budgetDao(),
        subscriptionDao = db.subscriptionDao(),
        billDao = db.billDao()
    )

    /** Real user request (2026-08, forwarded suggestion): "let the app proactively answer
     * questions like 'why did I spend more this month'" - see FinanceQaEngine's kdoc. Neither
     * interpreter (rule-based or AI) recognizes a genuine question as one of the known action
     * types, so it lands here exactly like any other unparseable input already did; the only
     * change is trying a real answer first instead of immediately giving up. Falls back to the
     * same help text as before if AI isn't available (blank/invalid key, network failure, offline)
     * - there's no honest deterministic way to answer an open-ended question the way the regex
     * fallback answers a structured action. */
    private suspend fun unrecognized(intent: CommandIntent.Unrecognized, history: List<ConversationTurn>): String {
        val rawText = intent.rawText.trim()
        if (rawText.isNotBlank()) {
            // Monetization scaffolding (2026-08-12) - this is the ONE AI touchpoint gated behind
            // the free-tier quota, deliberately: FinanceQaEngine.answer is the proactive
            // financial Q&A feature ("why did I spend more this month", forecasting-adjacent
            // questions), the "unlimited AI chat" the paid tier is meant to unlock. Structured
            // command interpretation above (logging an expense by typing "spent 200 on lunch")
            // and the SMS-parsing AI fallback are deliberately NOT gated here - those support the
            // core free tracking experience, not an insights feature someone would pay for, and
            // gating them would make the free tier feel broken rather than just less generous.
            val quotaAvailable = Prefs.isPremium(context) ||
                Prefs.aiQuestionsUsedThisMonth(context) < Prefs.FREE_AI_QUESTIONS_PER_MONTH
            if (quotaAvailable) {
                FinanceQaEngine.answer(rawText, db, Prefs.getAiLanguage(context), history)?.let { answer ->
                    if (!Prefs.isPremium(context)) Prefs.recordAiQuestionUsed(context)
                    return answer
                }
            } else {
                return "You've used all ${Prefs.FREE_AI_QUESTIONS_PER_MONTH} free AI questions this month - " +
                    "upgrade to Premium for unlimited questions, or this resets next month."
            }
        }
        return "I didn't recognize that yet - try things like \"spent 200 on lunch\", \"add task call mom tomorrow\", " +
            "\"add habit meditate\", \"add milk to shopping\", \"complete task call mom\", " +
            "\"mark meditate done\", \"check off milk\", \"confirm bill electricity\", \"set food budget to 5000\", " +
            "or ask a question like \"why did I spend more this month\" (needs AI configured)."
    }
}
