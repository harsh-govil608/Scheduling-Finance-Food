# Architecture Review — Pre-Beta Hardening Pass

**Scope:** Priority 5 of the staff-engineer beta-readiness pass - identify large classes, improve
separation of concerns, remove technical debt from rapid feature development, improve naming and
documentation. One concrete refactor; the rest is a proportionality call on what else genuinely
needed the same treatment.

---

## 1. File-size audit

```
find app/src/main/java -name "*.kt" -exec wc -l {} \; | sort -rn | head
```

Before this pass, `NotificationCheckWorker.kt` was the clear outlier at **555 lines** - more than
150 lines larger than the next-biggest file in the codebase. It had grown from roughly 5-6 checks
at its original scope to 12, one per proactive signal added across this session's feature work
(bills, subscriptions, budgets, budget pace, tasks, tasks-due-soon, habits, habits-at-risk, night
summary, morning heads-up, goals-off-track, plus the bill-to-task sync), all as private methods on
one `CoroutineWorker` class.

Every other file in the codebase sits at or below ~390 lines, and the next largest (`HomeScreen.kt`,
`FinanceInsightsRepository.kt`) are Compose UI screens and a single-pillar repository respectively -
neither is a grab-bag of unrelated logic the way `NotificationCheckWorker.kt` was; they're one
cohesive concern that happens to have some length to it. Splitting a Compose screen carries a real
regression risk this brief explicitly doesn't want (state hoisting and recomposition scope are easy
to get subtly wrong in a purely mechanical split, unlike moving a suspend function's body verbatim
into a new file), for no separation-of-concerns benefit - it's already one concern. That's why this
pass's one concrete action is scoped to `NotificationCheckWorker.kt` alone.

## 2. Fixed: `NotificationCheckWorker.kt` split by pillar

**Before:** one 555-line class mixing Finance checks (bills, subscriptions, budgets), Home checks
(tasks, habits, goals), cross-pillar checks (night summary, morning heads-up), WorkManager
scheduling, and the concurrency guard, all as private methods with no grouping beyond call order in
`doWork()`.

**After:** three new objects, grouped exactly the way the app's own pillars are (Finance / Home /
neither), plus a thin orchestrator:

- `FinanceNotificationChecks.kt` (182 lines) - `checkBills`, `checkSubscriptions`, `checkBudgets`,
  `checkBudgetPace`, `syncBillTasks`.
- `ProductivityNotificationChecks.kt` (198 lines) - `checkTasks`, `checkTasksDueSoon`, `checkHabits`,
  `checkHabitsAtRisk`, `checkGoalsOffTrack`.
- `CrossPillarNotificationChecks.kt` (87 lines) - `checkNightSummary`, `checkMorningHeadsUp`: the
  two checks that read across both pillars and don't belong to either one.
- `NotificationCheckWorker.kt` (146 lines) - scheduling (`schedulePeriodic`/`runOnce`), the
  `executionMutex`, and `doWork()` as a straight-line list of `runCheck("name") { Group.checkX(...) }`
  calls. Nothing about *what* each check does lives here anymore - only *that* it runs, in what
  order, under what guard.

**Why this reduces production risk:** the previous single-file shape meant any change to, say,
Habits logic required scrolling past Bills, Subscriptions, and Budget code to find it, and a merge
conflict in one pillar's check could spuriously conflict with another pillar's unrelated edit in the
same file. Grouping by pillar means a Finance-only change now touches only
`FinanceNotificationChecks.kt` (plus the one-line wiring in the orchestrator if a check is
added/removed), matching the boundary the rest of the app already uses (separate ViewModels,
screens, and repositories per pillar). This is a pure relocation - every function body, constant,
and kdoc comment moved verbatim; nothing was rewritten, reordered within `doWork()`, or changed in
behavior.

**Verification:** every constant used by a moved function (`DUE_SOON_WINDOW_DAYS`,
`TASK_DUE_SOON_WINDOW_MILLIS`, `MORNING_WINDOW_START/END_HOUR`, `MIN_COMPLETIONS_FOR_RHYTHM`,
`MIN_GAP_DAYS_FOR_RISK_CHECK`, `RISK_GAP_MULTIPLIER`, `GOAL_ON_TRACK_RATIO`, `DATE_FORMATTER`)
was traced to exactly the new file that uses it, with no duplicates and nothing left orphaned in
the old file. Every call site in the new `doWork()` was checked against the target function's
signature by hand (parameter order and types), and a repo-wide grep confirmed no test or other
production file called any of the now-relocated private methods directly (they were `private`, so
this was structurally guaranteed, not just likely). No Gradle/Android SDK CLI tooling is available
in this environment (no `gradlew` wrapper is checked in, and no system-wide `gradle` install exists
in the shell's PATH), so this could not be closed out with an automated `compileDebugKotlin` +
`testDebugUnitTest` run the way P1-P4's changes were. **A Kotlin compile and the existing unit test
suite should be run once from Android Studio before this ships**, as the authoritative check this
review couldn't perform directly.

## 3. Reviewed, not changed: naming and documentation elsewhere

Two kdoc comments in other files referenced the moved functions by their old path
(`NotificationCheckWorker.syncBillTasks`) - in `TaskEntity.kt` (explaining `sourceBillId`) and
`HomeViewModel.kt` (explaining why Subscriptions are excluded from the cash-flow guard's bill-task
bridge). Both updated to `FinanceNotificationChecks.syncBillTasks` so the documentation still
points somewhere real. No other file referenced the moved functions by name outside of prose
comments in `CrashLogEntity.kt` and `NotificationDao.kt` that name `NotificationCheckWorker`
generically (as the source of crash breadcrumbs / cooldown checks) rather than any specific
function - still accurate, since the class is still where those things run from.

## 4. Reviewed, not changed: naming conventions and remaining technical debt

- Naming across the codebase is already consistent (pillar-prefixed ViewModels/screens, `*Entity`/
  `*Dao`/`*Engine`/`*Repository` suffixes applied uniformly) - no renames were identified as
  necessary.
- The one already-documented duplication (the "current monthly pace from net cash flow" estimate,
  duplicated between `SpendingInsightEngine` and, after this split, `ProductivityNotificationChecks
  .checkGoalsOffTrack`) is unchanged from the reliability review's finding: two ~4-line call sites,
  deliberately left un-extracted per this project's standing discipline against premature
  abstraction. Flagged again here only because the file it lives in changed name, not because the
  underlying call has changed.
- No other class was found carrying multiple unrelated responsibilities the way
  `NotificationCheckWorker.kt` was - the rest of the codebase's size distribution (see the audit in
  §1) reflects genuine per-concern length, not debt.

## 5. What changed as a direct result of this review

- `NotificationCheckWorker.kt` - reduced from 555 to 146 lines; now an orchestrator only.
- `FinanceNotificationChecks.kt`, `ProductivityNotificationChecks.kt`,
  `CrossPillarNotificationChecks.kt` - new files, one per pillar grouping, holding the 12
  relocated check functions verbatim.
- `TaskEntity.kt`, `HomeViewModel.kt` - two kdoc references updated to the functions' new location.

No production behavior changed - same checks, same order, same cooldowns, same Mutex guard. This
was a pure structural move to make the notification-check code's organization match the pillar
boundary the rest of the app already uses.
