# Coder's Documentation — Day 5

**Scope of Day 5:** the first four items from the AI Transformation Plan's build order - all
deliberately the ones needing zero model of any kind, since that's genuinely most of what's
actually missing (see the plan's own ranking: 10 of 12 recommendations are rule engines, not
model calls). Each closes a real gap between two modules that already had the right numbers
computed but never compared them to each other.

- [1. H1 — Bill-to-Task Auto-Creation](#1-h1--bill-to-task-auto-creation)
- [2. F1 — Cross-Module Cash-Flow Guard](#2-f1--cross-module-cash-flow-guard)
- [3. P1 — Personalized First-Scan Onboarding Summary](#3-p1--personalized-first-scan-onboarding-summary)
- [4. F2 — Recurring Pattern Intelligence, Generalized](#4-f2--recurring-pattern-intelligence-generalized)
- [5. Schema Changes](#5-schema-changes)
- [6. Verification Status](#6-verification-status)

---

## 1. H1 — Bill-to-Task Auto-Creation

Finance always knew a bill was coming due; Home's Tasks had no idea it existed - a bill generated
a push notification and nothing else, never became a thing to actually go *do*. `NotificationCheckWorker.syncBillTasks()`
bridges the two: within a 3-day window of a `CONFIRMED_TRACKED` bill's due date, it ensures a
linked task exists on Home's own Due Today list ("Pay Jio Fiber (~₹899)"), updating the same task
in place across checks rather than spawning a duplicate every 6-hour run.

Scoped to Bills only, deliberately excluding Subscriptions - a subscription is auto-debited (see
`SubscriptionEntity`'s own kdoc), so a "pay Netflix" task would be a false action item; Bills
(Doc 22) are explicitly the variable-amount, user-actioned kind the PRD itself distinguishes them
by. The loop closes both directions: completing a bill-generated task marks the underlying bill's
`lastPaidDate`, so it stops being flagged as due and the sync worker won't recreate it for the
same cycle.

## 2. F1 — Cross-Module Cash-Flow Guard

Budgets and Bills each already showed correct numbers, but never cross-checked against each other -
a user could look "on pace" in Budgets while several bills and subscription renewals landed the
same week. `HomeViewModel.computeCashFlowRisk()` projects known due-dates (Bills + Subscriptions,
including overdue amounts still owed) within a 14-day window against total remaining budget
headroom at current pace, and surfaces a new `AttentionItem.CashFlowRisk` on Finance home when the
upcoming total exceeds what's left.

Deliberately returns no signal at all when zero budgets exist - there's nothing to project a pace
against, and a false "you're fine" would be worse than silence. Slotted into the existing
"needs attention" precedence between a single overdue bill (most concrete, most actionable) and a
plain over-budget category (retrospective, informational).

## 3. P1 — Personalized First-Scan Onboarding Summary

The onboarding completion screen said "you're all set, we've scanned your messages" regardless of
what the scan actually found - the single most fragile retention moment in the app got the least
personalized message anywhere in it. It now reports the real numbers: transaction count, the
biggest spending category so far with its total, and how many recurring bills/subscriptions were
already detected. A genuinely empty scan gets an honest, non-deflating framing ("Found 0 so far -
more will show up automatically") rather than a hollow "all set."

Recurring detection now runs once more, right before this screen builds its summary, so "N look
like a recurring bill or subscription" is accurate on a first-ever scan rather than always reading
zero.

## 4. F2 — Recurring Pattern Intelligence, Generalized

The existing `RecurringPatternDetector` only ever asked "does this repeat," never "did this
change." Two additions, reusing the same interval/variance-averaging shape rather than the same
code (the two domains - transaction amounts, shopping-item cadence - didn't share enough surface
to justify forcing one abstraction over both):

- **Subscriptions now flag a price drift.** `FinanceInsightsRepository.observeSubscriptions()`
  compares a merchant's most recent charge against the average of every prior one; a swing past
  20% ("Netflix usually charges ₹199 - the last charge was ₹649") surfaces directly on the
  Subscriptions screen, computed at display time from real transaction history, not persisted.
- **Shopping now suggests items that are "about due."** `ShoppingViewModel` groups every item ever
  checked off (checked items are never auto-deleted, so history accumulates) by name, and for a
  name that repeats on a real cadence but isn't currently on the active list, surfaces a soft
  `AssistChip` - "Milk · last bought 6 days ago." Tapping it adds the item back exactly the normal
  way; it is never auto-added.

---

## 5. Schema Changes

Two more real migrations, continuing the discipline `MIGRATION_7_8` started - every schema change
gets a hand-written `Migration`, not a reliance on `fallbackToDestructiveMigration()`:

- **`MIGRATION_8_9`** adds `tasks.sourceBillId` (nullable, no FK constraint - same pattern as the
  existing `projectId` column), the link H1's sync worker uses to update a bill-generated task in
  place.
- **`MIGRATION_9_10`** adds `shopping_items.checkedAt`, the timestamp F2's suggestion engine reads
  to compute a buying cadence per item name.

## 6. Verification Status

Everything in this batch compiles clean (`compileDebugKotlin`) and passes the existing unit test
suite (`testDebugUnitTest`) - no test device was connected during this session, so none of it has
been installed and verified on-device yet, and nothing has been shipped to `distribution/`. Per
this project's own established discipline (see Day 3's migration-verification notes), the two new
migrations specifically should be checked byte-for-byte on a real device - pull the DB before/after
installing as an update, not a fresh install - before this batch goes to a pilot tester.
