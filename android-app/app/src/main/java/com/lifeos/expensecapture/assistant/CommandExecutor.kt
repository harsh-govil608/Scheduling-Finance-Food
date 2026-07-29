package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.first

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
        val insights = FinanceInsightsRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao(),
            subscriptionDao = db.subscriptionDao(),
            billDao = db.billDao()
        )
        insights.setBudget(categoryId, intent.monthlyLimit)
        val label = categoryId?.let { id -> categories.firstOrNull { it.id == id }?.name } ?: "Overall"
        return "Set $label budget to ₹${"%.2f".format(intent.monthlyLimit)}."
    }

    private fun unrecognized(): String =
        "I didn't recognize that yet - try things like \"spent 200 on lunch\", \"add task call mom tomorrow\", " +
            "\"add habit meditate\", \"add milk to shopping\", or \"set food budget to 5000\"."
}
