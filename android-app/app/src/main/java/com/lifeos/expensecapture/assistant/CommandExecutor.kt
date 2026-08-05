package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Executes a CommandIntent by calling the exact same repository/DAO methods every existing
 * Add/Edit UI already calls - see CommandIntent.kt's kdoc for why this is a separate layer from
 * the interpreter. Returns a plain-language confirmation (or a clear explanation of what's
 * missing) rather than throwing, so the chat UI always has something to show the user.
 */
class CommandExecutor(private val db: AppDatabase) {

    suspend fun execute(intent: CommandIntent): String = when (intent) {
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
        is CommandIntent.Unrecognized -> unrecognized()
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

    private fun unrecognized(): String =
        "I didn't recognize that yet - try things like \"spent 200 on lunch\", \"add task call mom tomorrow\", " +
            "\"add habit meditate\", \"add milk to shopping\", \"complete task call mom\", " +
            "\"mark meditate done\", \"check off milk\", \"confirm bill electricity\", or \"set food budget to 5000\"."
}
