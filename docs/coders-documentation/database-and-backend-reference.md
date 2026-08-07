# Database & Backend Reference

Generated 2026-08 directly from the current codebase (not the earlier product-vision docs in
`phase-1-foundation/`/`phase-2-product-definition/`, which predate most of what's described here
and are no longer accurate to the actual implementation). Re-generate this doc rather than
hand-editing it stale — ask for "an updated database and backend reference doc" and point at the
current code.

Two independent data stores:
- **Room (local SQLite)** — everything in the Finance/Productivity pillars. Fully offline, never
  leaves the device.
- **Firebase Firestore (cloud)** — only the two cross-device features: **Family Sharing** and
  **Smart Split**. Everything else in the app works with zero network/account requirement.

---

## 1. Room Database (local, per-device)

File: `app/src/main/java/com/lifeos/expensecapture/data/db/AppDatabase.kt`
Current version: **15** · DB file: `expense_capture_pilot.db` · `exportSchema = false`

### 1.1 Tables

| Table | Entity file | What it stores |
|---|---|---|
| `transactions` | `TransactionEntity.kt` | The core expense ledger — SMS-auto-captured or manually entered transactions |
| `categories` | `CategoryEntity.kt` | Spending categories (system default + user-created) |
| `merchant_rules` | `MerchantRuleEntity.kt` | Learned/authored merchant → category mapping |
| `corrections` | `CorrectionEntity.kt` | Log of every manual recategorization (validation signal) |
| `unparsed_messages` | `UnparsedMessageEntity.kt` | SMS the parser couldn't extract a transaction from (Needs Review queue) |
| `budgets` | `BudgetEntity.kt` | Monthly limits, per-category or overall (`categoryId == null`) |
| `subscriptions` | `SubscriptionEntity.kt` | Detected/confirmed recurring subscriptions |
| `bills` | `BillEntity.kt` | Detected/confirmed recurring bills (variable amount, due day) |
| `app_notifications` | `NotificationEntity.kt` | Local Notification Center feed |
| `consents` | `ConsentEntity.kt` | Permission grant/deny state per permission type |
| `investments` | `InvestmentEntity.kt` | Manually entered, read-only investment holdings |
| `tasks` | `TaskEntity.kt` | To-do items (optional project link, optional auto-generated-from-bill link) |
| `habits` | `HabitEntity.kt` | Named habits being tracked |
| `habit_completions` | `HabitCompletionEntity.kt` | One row per day a habit was marked done |
| `projects` | `ProjectEntity.kt` | Named groups tasks can be tagged into |
| `goals` | `GoalEntity.kt` | Savings/target goals, optional target date + rupee amount |
| `notes` | `NoteEntity.kt` | Notes and journal entries (`type` field discriminates) |
| `shopping_items` | `ShoppingItemEntity.kt` | Shopping checklist items |
| `crash_logs` | `CrashLogEntity.kt` | On-device crash/handled-exception log (see Diagnostics screen) |
| `split_expenses` | `SplitExpenseEntity.kt` | Local single-device "I fronted this money" split header |
| `split_participants` | `SplitParticipantEntity.kt` | One row per person's share of a local split |

21 tables, 21 matching DAOs under `data/db/dao/` (one per table, same naming).

### 1.2 Column detail

**`transactions`** (`TransactionEntity`) — unique index on `sourceHash`
- `id: Long` PK · `amount: Double` · `direction: DEBIT|CREDIT` · `merchantRaw/merchantNormalized: String`
- `categoryId: Long` · `date: Long` (epoch millis) · `source: SMS_AUTO|MANUAL` · `confidenceScore: Float`
- `isUserCorrected: Boolean` · `createdAt: Long` · `synced: Boolean` (unused today, reserved)
- `sourceHash: String` — `"$sender::$body"` for SMS rows (the actual dedup key), random UUID for manual entries

**`categories`** — `id`, `name`, `isSystemDefault: Boolean`

**`merchant_rules`** — unique index on `merchantPattern`
- `id`, `merchantPattern: String`, `categoryId: Long`, `createdFromUserCorrection: Boolean`, `isManuallyAuthored: Boolean`, `isPaused: Boolean`

**`corrections`** — `id`, `transactionId`, `oldCategoryId`, `newCategoryId`, `correctedAt`

**`unparsed_messages`** — unique index on `sourceHash`
- `id`, `sender`, `body`, `receivedAt`, `reason: String`, `resolved: Boolean`, `sourceHash`

**`budgets`** — `id`, `categoryId: Long?` (null = overall), `monthlyLimit: Double`, `createdAt`

**`subscriptions`** — `id`, `merchantNormalized/merchantDisplay`, `amount`, `cadenceDays: Int`, `lastTransactionDate`, `status: DETECTED_UNCONFIRMED|CONFIRMED_TRACKED|CANCELLED`, `detectedAt`, `isManuallyAdded`

**`bills`** — `id`, `payeeNormalized/payeeDisplay`, `typicalAmount`, `dueDayOfMonth: Int`, `lastPaidDate: Long?`, `status: DETECTED_UNCONFIRMED|CONFIRMED_TRACKED|CANCELLED`, `detectedAt`, `isManuallyAdded`

**`app_notifications`** — `id`, `type: NotificationType` (14 values — `BILL_DUE`, `SUBSCRIPTION_RENEWAL`, `BUDGET_OVER_LIMIT`, `NIGHT_SUMMARY_READY`, `TASK_DUE`, `HABIT_REMINDER`, `TASK_DUE_SOON`, `MORNING_HEADSUP`, `BUDGET_PACE_WARNING`, `HABIT_AT_RISK`, `UNUSUAL_TRANSACTION`, `GOAL_OFF_TRACK`, `UNCATEGORIZED_SPEND`, `PERIODIC_CHECK_IN`), `title`, `body`, `deepLinkRoute`, `sourceKey: String` (dedup/cooldown key), `createdAt`, `isRead`, `isDismissed` (soft delete)

**`consents`** — PK is `permissionType: String` itself · `granted: Boolean`, `decidedAt`

**`investments`** — `id`, `name`, `currentValue: Double`, `createdAt`

**`tasks`** — `id`, `title`, `notes`, `priority: LOW|MEDIUM|HIGH`, `dueDate: Long?`, `completed: Boolean`, `completedAt: Long?`, `createdAt`, `projectId: Long?` (no FK constraint), `sourceBillId: Long?` (links to an auto-generated bill-payment task)

**`habits`** — `id`, `name`, `createdAt`, `archived: Boolean`

**`habit_completions`** — unique index on `(habitId, dateEpochDay)`
- `id`, `habitId`, `dateEpochDay: Long` (`LocalDate.toEpochDay()`, not millis — no time-of-day)

**`projects`** — `id`, `name`, `createdAt`, `archived: Boolean`

**`goals`** — `id`, `title`, `targetDate: Long?`, `completed: Boolean`, `completedAt: Long?`, `createdAt`, `targetAmount: Double?`

**`notes`** — `id`, `type: NOTE|JOURNAL`, `title`, `body`, `createdAt`, `updatedAt`

**`shopping_items`** — `id`, `name`, `quantity: String`, `checked: Boolean`, `createdAt`, `checkedAt: Long?`

**`crash_logs`** — `id`, `timestamp`, `fatal: Boolean`, `threadName`, `exceptionType: String`, `message: String?`, `stackTrace: String`, `appVersionName`, `source: String?` (breadcrumb tag)

**`split_expenses`** — `id`, `description`, `totalAmount`, `date`, `createdAt`

**`split_participants`** — `id`, `splitExpenseId: Long` (no FK constraint), `name: String` (free text, no contact link), `shareAmount`, `settled: Boolean`, `settledAt: Long?`

### 1.3 Migration history

8 real migrations (`MIGRATION_7_8` through `MIGRATION_14_15`); everything before version 7 used `fallbackToDestructiveMigration()` only. That fallback is still registered alongside the real migrations as a safety net.

| Migration | What it did |
|---|---|
| 7→8 | Added `notes` and `shopping_items` tables |
| 8→9 | Added `sourceBillId` to `tasks` |
| 9→10 | Added `checkedAt` to `shopping_items` |
| 10→11 | Added `targetAmount` to `goals` |
| 11→12 | Added `crash_logs` table |
| 12→13 | Deduped `merchant_rules`, added unique index on `merchantPattern` |
| 13→14 | Added `isDismissed` to `app_notifications` |
| 14→15 | Added `split_expenses` + `split_participants` tables |

---

## 2. Firebase Firestore (cloud, cross-device)

Two independent modules sharing one Firebase project and one Auth identity system, but otherwise
unrelated collection trees.

### 2.1 Family Sharing module

```
families/{familyId}
families/{familyId}/members/{userId}
families/{familyId}/invitations/{inviteId}
families/{familyId}/events/{eventId}            - FamilyEvent (AI-ready activity stream)
families/{familyId}/tasks/{taskId}              - SharedTask
families/{familyId}/calendarEvents/{eventId}    - SharedCalendarEvent
families/{familyId}/expenses/{expenseId}        - SharedExpense
families/{familyId}/documents/{documentId}      - SharedDocument
families/{familyId}/healthRecords/{recordId}    - HealthRecord
families/{familyId}/emergencyContacts/{id}      - EmergencyContact
families/{familyId}/sosAlerts/{alertId}         - SOSAlert
families/{familyId}/notifications/{id}          - FamilyNotification
families/{familyId}/presence/{userId}           - MemberPresence
families/{familyId}/ledger/{id}                 - FamilyLedgerEntry (auto-synced from local SMS capture)
```

| Collection | Key fields | Owning repository |
|---|---|---|
| `families/{familyId}` | `name, ownerId, memberIds: List<String>` | `FamilyRepository` |
| `.../members/{userId}` | `displayName, role: OWNER\|PARENT\|ADULT\|CHILD\|GUEST, permissions: PermissionSet` | `FamilyRepository` |
| `.../invitations/{id}` | `code, proposedRole, expiresAt, status: PENDING\|ACCEPTED\|EXPIRED` | `FamilyRepository` |
| `.../events/{id}` | `type, actorId, actorName, timestamp, payload: Map<String,String>` | `EventStreamRepository` |
| `.../tasks`, `.../calendarEvents`, `.../expenses`, `.../documents`, `.../healthRecords`, `.../emergencyContacts` | (module-specific) | `SharedTaskRepository` etc. — thin facades over generic `FamilyCollectionRepository<T>`, all in `family/data/SharedModuleRepositories.kt` |
| `.../sosAlerts/{id}` | `triggeredByUserId, location: GeoPoint?, status: ACTIVE\|RESOLVED` | `SosRepository` |
| `.../notifications/{id}` | `type, title, body, relatedUserId?, read` (shared family feed, not per-recipient inboxes) | `FamilyNotificationRepository` |
| `.../presence/{userId}` | `status: ONLINE\|OFFLINE\|AWAY, lastLocation: GeoPoint?` | `PresenceRepository` |
| `.../ledger/{id}` | `memberName, merchantName, amount, direction: String, categoryName, date` | `FamilyLedgerRepository` |

### 2.2 Smart Split module

```
users/{uid}                                          - UserPayProfile
users/{uid}/splitHistory/{id}                        - SplitHistoryEntry (30-day auto-pruned)
smartSplits/{splitId}                                - SmartSplit
smartSplits/{splitId}/participants/{participantId}   - SmartSplitParticipant
```

Deliberately **not** nested under `families/` — same Firebase project and auth identity, separate
top-level module. `participants` is a subcollection (not an embedded array) specifically so the
external Track-B web page (`docs/pay/index.html`) can update one participant's status with a
narrowly-scoped security rule, instead of needing write access to the whole split.

All owned by `SplitPayRepository` (`splitpay/data/`).

### 2.3 Firestore indexes

`firestore.indexes.json` at repo root — 2 collection-group indexes, both single-field ascending:
1. `participants.participantUserId` — needed for "which splits do I owe money in" (a cross-split
   collection-group query)
2. `invitations.code` — needed for joining a family by code (cross-family lookup)

No composite indexes exist. Several repositories deliberately avoid needing one by sorting
client-side instead of chaining `whereEqualTo` + `orderBy` on different fields (`SplitPayRepository
.observeMySplits`, `SosRepository.observeActiveAlerts`).

**Security rules** are managed in Firebase Console, not version-controlled in this repo (a draft
`firestore.rules` exists locally, unpublished — see the 2026-08 security-hardening thread if
resuming that work).

---

## 3. Firebase Storage

`family/data/DocumentStorageRepository.kt` — only user of Storage in the app.
Path: `families/{familyId}/documents/{uuid}-{fileName}`. Powers the Documents shared module only;
the download URL is stored back on the paired `SharedDocument.storageUrl` Firestore field.

---

## 4. AI provider

`assistant/AiClient.kt` — single shared entry point (`AiClient.generateText()`) every AI feature
in the app goes through (Assistant tab, insight-card polishing, categorization suggestions,
bill/budget review, SMS-parsing fallback).

- Backed by **OpenRouter** (OpenAI-compatible chat completions), `https://openrouter.ai/api/v1/`
- Model: `openai/gpt-oss-20b:free`
- Key: `BuildConfig.OPENROUTER_API_KEY`, sourced from `local.properties` (gitignored, never committed)
- Fails soft (`null`) on any error — every caller has a deterministic fallback; the app works
  fully offline without this configured at all

## 5. Other Firebase services (`app/build.gradle.kts`)

| Service | Used for |
|---|---|
| `firebase-firestore-ktx` | Family + Smart Split realtime data |
| `firebase-auth-ktx` | Family phone+OTP identity, Smart Split anonymous auth |
| `firebase-messaging-ktx` | Declared for SOS/reminder push - **actual delivery still needs an FCM Cloud Function, not yet built** |
| `firebase-storage-ktx` | Documents module uploads |
| `firebase-crashlytics-ktx` | Remote crash reporting (added 2026-08), alongside the local `crash_logs` table |
| `play-services-location` | SOS trigger + presence location capture |
