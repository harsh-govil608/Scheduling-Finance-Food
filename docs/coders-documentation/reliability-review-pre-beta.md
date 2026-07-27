# Reliability Review — Pre-Beta Hardening Pass

**Scope:** Priority 4 of the staff-engineer beta-readiness pass - async code, race conditions,
error handling, retry strategy, duplicated logic. Two real, fixed issues; the rest is a
proportionality call on what else genuinely needed the same treatment versus what's noted as a
recommendation.

---

## 1. Fixed: a real race condition in the periodic worker's trigger

**Severity: Medium - a real, if narrow, source of duplicate data**

`NotificationCheckWorker.runOnce()` enqueued a plain, unnamed one-time WorkManager request on
every call - and it was called from `HomeViewModel.init`, meaning **every time the Finance-tab
composable entered composition** (opening Home, switching tabs back and forth, a config change
recomposing the screen). Nothing prevented two of these from running fully concurrently, or from
running concurrently with the periodic (every-2h) worker.

That's a genuine check-then-act race: `syncBillTasks()` looks up "is there already a task for
this bill" before deciding to insert one. Two concurrent executions can both see "no task yet"
before either one's insert commits, producing a real duplicate task. The same shape of risk
existed for notification records via `recentlyNotified()`.

**Fix:**
- `runOnce()` now uses `enqueueUniqueWork(name, ExistingWorkPolicy.KEEP, ...)` - a trigger that
  arrives while one is already pending/running is dropped, not queued or run redundantly. The
  check still happens soon either way; this only removes the *redundant* concurrent runs.
- A `Mutex` around the worker's entire `doWork()` body closes the rarer remaining case (the
  periodic worker and a run-once request happening to fire in the same moment - different unique
  names, so WorkManager alone doesn't serialize them). A second execution simply waits; by the
  time it runs, the first's writes are already committed, so its own idempotency checks correctly
  see "already handled."

No behavior change on the success path - the same checks still run, just never redundantly or
interleaved.

## 2. Fixed: an unguarded coroutine that runs on every single app launch

**Severity: Medium - a real boot-loop risk, not yet observed but structurally real**

`App.onCreate()` launches a coroutine (in a `SupervisorJob`-scoped `applicationScope`) that
seeds default categories and adopts any pending crash file. Before this pass, it had no
try/catch. An uncaught exception in a `SupervisorJob` child coroutine still propagates to the
thread's default uncaught-exception handler - which, after Priority 2's changes, is now
`CrashHandler`. Meaning: a transient failure here (e.g., a momentary DB issue) would crash the
app **on every subsequent launch**, since this code runs unconditionally at startup, before the
user can reach any screen - including the new Diagnostics screen meant to explain what went
wrong.

**Fix:** wrapped in try/catch, logged via `AppLogger.e()` on failure, app continues starting up
either way (worst case: categories don't get seeded that one time, which is far better than the
app being unable to launch at all).

## 3. Reviewed, not changed: every other ViewModel/worker coroutine

**Severity: Low individually; a systemic pattern worth naming**

Every `viewModelScope.launch { ... }` across the app's ~20 ViewModels (task/habit/goal/shopping
CRUD, etc.) has the same theoretical property: an uncaught exception propagates to the same
global handler. Individually, each of these is low-risk (a single failed action, not a
startup-blocking one) and there are simply too many call sites to hand-wrap all of them in this
pass without it becoming the kind of broad, mechanical rewrite this brief explicitly asked to
avoid ("do not modify production behavior unless a bug is discovered" - a single ViewModel
action failing occasionally isn't itself a discovered bug in any of them individually).

**Recommendation, not done here:** a shared `ViewModel.launchSafely { }` extension (wrapping
`viewModelScope.launch` with a try/catch + `AppLogger.e()`, same shape as `NotificationCheckWorker.runCheck()`)
would close this systemically in one small utility, applied gradually as each ViewModel is next
touched, rather than a single large mechanical diff across the whole app right now.

## 4. Reviewed: no other unguarded async entry points of comparable severity

- `UpdateChecker.checkForUpdate()` already has its own try/catch (offline/unreachable/malformed
  manifest all handled, pre-existing).
- `ParseIncomingSmsWorker` and `NotificationCheckWorker` - covered under Priority 2.
- No other `CoroutineWorker`, `BroadcastReceiver`, or `Application`-level coroutine exists in the
  codebase.

## 5. Duplicated logic

The "current monthly pace from net cash flow" calculation appears twice: once in
`SpendingInsightEngine.computeGoalAcceleration()`, once in
`NotificationCheckWorker.checkGoalsOffTrack()`. Both are ~4 lines and were left duplicated
deliberately rather than extracted into a shared utility for two call sites - matching this
project's own established discipline against premature abstraction. Flagged here for
visibility, not fixed: if a third caller ever needs the same estimate, that's the point to
extract it.

No other meaningful logic duplication was found - the two big consolidations (`NotificationSender`
shared between the two workers, `RecurringPatternDetector` shared between Subscriptions and
Bills) already existed or were done as part of this session's earlier feature work.

## 6. Verified: division-by-zero / edge-case guards in this session's newest math

Every rate/pace calculation added this session already guards its denominator before dividing
(`daysElapsed.coerceAtLeast(1.0)` in three separate places; explicit `<= 0.0` checks before using
a computed pace as a divisor in both `SpendingInsightEngine` and `checkBudgetPace`). No crash-risk
division found; the unit tests added in Priority 1 exercise these exact boundaries directly.

## 7. What changed as a direct result of this review

- `NotificationCheckWorker.runOnce()` - unique work + `ExistingWorkPolicy.KEEP`.
- `NotificationCheckWorker.doWork()` - wrapped in a `Mutex`.
- `App.onCreate()`'s startup coroutine - wrapped in try/catch, logged.

Everything else above is a finding or a recommendation, not a silent behavior change.
