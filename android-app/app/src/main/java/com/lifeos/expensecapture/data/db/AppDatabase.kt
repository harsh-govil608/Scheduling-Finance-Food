package com.lifeos.expensecapture.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeos.expensecapture.data.db.dao.BillDao
import com.lifeos.expensecapture.data.db.dao.BudgetDao
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.ConsentDao
import com.lifeos.expensecapture.data.db.dao.CorrectionDao
import com.lifeos.expensecapture.data.db.dao.CrashLogDao
import com.lifeos.expensecapture.data.db.dao.GoalDao
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.InvestmentDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.dao.NoteDao
import com.lifeos.expensecapture.data.db.dao.NotificationDao
import com.lifeos.expensecapture.data.db.dao.ProjectDao
import com.lifeos.expensecapture.data.db.dao.ShoppingItemDao
import com.lifeos.expensecapture.data.db.dao.SubscriptionDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.dao.UnparsedMessageDao
import com.lifeos.expensecapture.data.db.entity.BillEntity
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.BudgetEntity
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.ConsentEntity
import com.lifeos.expensecapture.data.db.entity.CorrectionEntity
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import com.lifeos.expensecapture.data.db.entity.NoteEntity
import com.lifeos.expensecapture.data.db.entity.NoteType
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.data.db.entity.ProjectEntity
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionStatus
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TaskPriority
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import com.lifeos.expensecapture.sms.SmsHistoryScanner

class Converters {
    @TypeConverter
    fun fromDirection(value: TransactionDirection): String = value.name

    @TypeConverter
    fun toDirection(value: String): TransactionDirection = TransactionDirection.valueOf(value)

    @TypeConverter
    fun fromSource(value: TransactionSource): String = value.name

    @TypeConverter
    fun toSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun fromSubscriptionStatus(value: SubscriptionStatus): String = value.name

    @TypeConverter
    fun toSubscriptionStatus(value: String): SubscriptionStatus = SubscriptionStatus.valueOf(value)

    @TypeConverter
    fun fromBillStatus(value: BillStatus): String = value.name

    @TypeConverter
    fun toBillStatus(value: String): BillStatus = BillStatus.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromNoteType(value: NoteType): String = value.name

    @TypeConverter
    fun toNoteType(value: String): NoteType = NoteType.valueOf(value)
}

/**
 * The first real Migration in this project - every schema change before this one used
 * `fallbackToDestructiveMigration()`, an accepted pilot-stage tradeoff while only auto-derived
 * SMS transactions were at stake (they self-heal via SmsHistoryScanner's catch-up rescan - see
 * docs/coders-documentation/day-3.md). That stopped being acceptable once the Home pillar
 * (Day 4) introduced manually-entered data - tasks, habits, goals, projects - with no SMS to
 * re-derive them from. This migration only ADDS the two new tables Notes/Journal and Shopping
 * need; every existing table and its data is left untouched.
 *
 * `fallbackToDestructiveMigration()` is still registered below as a safety net for any gap this
 * migration doesn't cover (there shouldn't be one, but a crash-on-open is a worse failure mode
 * than a wipe for a pilot with few users) - `addMigrations` takes precedence for the exact
 * version pair it defines, so this specific 7->8 transition is protected either way.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notes` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `type` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_items` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL,
                `quantity` TEXT NOT NULL,
                `checked` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * AI Transformation Plan H1 (bill-to-task auto-creation): tasks generated from a Bill need a
 * stable link back to it (see TaskEntity.sourceBillId's kdoc) so the sync worker can update the
 * same task in place instead of spawning a new one every check. A plain nullable column, no FK
 * constraint - consistent with how TaskEntity.projectId already links to Projects in this schema.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN sourceBillId INTEGER")
    }
}

/** AI Transformation Plan F2 (recurring pattern intelligence, generalized): shopping items need
 * a checked-timestamp (see ShoppingItemEntity.checkedAt's kdoc) to detect an "about due" pattern
 * from repeated check events, the same way subscriptions/bills are detected from repeated
 * transactions. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shopping_items ADD COLUMN checkedAt INTEGER")
    }
}

/** Spending Insight card: a Goal needs a real rupee target for "cutting X/day gets you there N
 * months sooner" to be an honest computed number rather than an invented one. */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE goals ADD COLUMN targetAmount REAL")
    }
}

/** Pre-beta hardening (Priority 2): local crash/handled-exception log - see CrashLogEntity's
 * kdoc for why this exists and why it never leaves the device. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `crash_logs` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestamp` INTEGER NOT NULL,
                `fatal` INTEGER NOT NULL,
                `threadName` TEXT NOT NULL,
                `exceptionType` TEXT NOT NULL,
                `message` TEXT,
                `stackTrace` TEXT NOT NULL,
                `appVersionName` TEXT NOT NULL,
                `source` TEXT
            )
            """.trimIndent()
        )
    }
}

/**
 * Bug fix (found via a real user report, 2026-07): merchant_rules had no way to enforce "one
 * rule per merchant pattern" - see MerchantRuleEntity's kdoc for the full story. Existing
 * installs may already have accumulated contradictory duplicate rules for the same merchant
 * (e.g. correcting a mis-tap creates a second row instead of replacing the first), so the
 * unique index can't just be added - it would fail to create over existing duplicate data.
 * This keeps only the most recently created rule per pattern (highest id = latest correction,
 * matching what the user most recently told the app) before adding the index that prevents the
 * duplication from recurring.
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM merchant_rules WHERE id NOT IN (
                SELECT MAX(id) FROM merchant_rules GROUP BY merchantPattern
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_merchant_rules_merchantPattern` ON `merchant_rules` (`merchantPattern`)"
        )
    }
}

/** Bug fix (found via a real user report, 2026-07): the Notification Center had no way to
 * remove anything - see NotificationEntity's isDismissed kdoc for why this is a soft delete
 * rather than a real DELETE (the cooldown check in NotificationSender.recentlyNotified still
 * needs the row). Defaults to false so every existing notification stays visible after update. */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_notifications ADD COLUMN isDismissed INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        MerchantRuleEntity::class,
        CorrectionEntity::class,
        UnparsedMessageEntity::class,
        BudgetEntity::class,
        SubscriptionEntity::class,
        BillEntity::class,
        NotificationEntity::class,
        ConsentEntity::class,
        InvestmentEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        ProjectEntity::class,
        GoalEntity::class,
        NoteEntity::class,
        ShoppingItemEntity::class,
        CrashLogEntity::class
    ],
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    abstract fun correctionDao(): CorrectionDao
    abstract fun unparsedMessageDao(): UnparsedMessageDao
    abstract fun budgetDao(): BudgetDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun billDao(): BillDao
    abstract fun notificationDao(): NotificationDao
    abstract fun consentDao(): ConsentDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun projectDao(): ProjectDao
    abstract fun goalDao(): GoalDao
    abstract fun noteDao(): NoteDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun crashLogDao(): CrashLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_capture_pilot.db"
                )
                    // Real migration path for every version gap so far (see each Migration's
                    // own kdoc). Destructive fallback stays as a safety net for any gap that
                    // isn't covered - there shouldn't be one, but see MIGRATION_7_8's kdoc for
                    // why a wipe is still preferable to a crash-on-open as a last resort.
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            SmsHistoryScanner.resetScanFlag(context.applicationContext)
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
