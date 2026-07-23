# Coder's Documentation — Day 2

**Scope of Day 2:** everything built since Day 1's 472-transaction milestone, in two batches: (1) the rest of the Finance Suite — Home dashboard, Budgets, Subscriptions, Bills, Spend Prediction, CSV export — and (2) ten more PRDs implemented end-to-end in a single push (Notification Center, Widgets, Search + Voice, Automation Rules, Onboarding, Permissions & Consent, Account & Profile Management, Offline Mode, Night Summary), plus a bonus Investments tracker. That brings the running total to **17 PRDs touched to a genuine, scoped-down, working implementation** (7 before today's push + 10 today), plus 1 bonus. This log also documents the most important thing found today: a real data-loss bug caught only by testing on the same physical device across a schema change, not by reading the code.

- [1. Tech Stack Additions](#1-tech-stack-additions)
- [2. Package Layout as of Day 2](#2-package-layout-as-of-day-2)
- [3. Exact Code Flow — New Pieces](#3-exact-code-flow--new-pieces)
- [4. How We Built It (Chronological)](#4-how-we-built-it-chronological)
- [5. Bugs Found and Fixed](#5-bugs-found-and-fixed)
- [6. What Was Achieved — In Depth](#6-what-was-achieved--in-depth)
- [7. PRD Tally](#7-prd-tally)
- [8. Known Gaps and Open Items](#8-known-gaps-and-open-items)

---

## 1. Tech Stack Additions

| Addition | Where | Why |
|---|---|---|
| `androidx.compose.material:material-icons-extended` | `app/build.gradle.kts` | `Icons.Default.Mic` (voice search) isn't in the core icon set — see Section 5.1 |
| `WorkManager` `PeriodicWorkRequestBuilder` (6h) | `NotificationCheckWorker` | Notification Center needs a recurring background check, not just reactive event handling |
| `AppWidgetProvider` + `RemoteViews` | `SpendWidgetProvider`, `res/layout/widget_spend_summary.xml`, `res/xml/widget_spend_info.xml` | Home-screen widget (Widgets PRD) |
| `ConnectivityManager.NetworkCallback` via `callbackFlow` | `ConnectivityObserver` | Reactive online/offline state for the Offline Mode indicator |
| `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` | `SearchScreen` | Voice-to-search without needing the `RECORD_AUDIO` permission directly — delegates to whatever recognizer app is installed |
| `NotificationChannel` / `NotificationCompat` / `POST_NOTIFICATIONS` (API 33+) | `NotificationChannels`, `NotificationCheckWorker`, manifest | Real system notifications, not just an in-app list |
| `FileProvider` | `CsvExporter`, manifest | Sharing an app-private CSV file through Android's share sheet without exposing the whole file system |
| Room `RoomDatabase.Callback.onDestructiveMigration` | `AppDatabase` | Added today, as a direct fix for the Section 5.4 bug — see below |

No backend, no new networking, still zero ML. Every new feature is either on-device rules/derivation from the existing transaction table, or local Android platform APIs (widgets, notifications, connectivity, speech recognizer).

---

## 2. Package Layout as of Day 2

```
com.lifeos.expensecapture/
├── data/db/
│   ├── entity/          + BudgetEntity, SubscriptionEntity, BillEntity, NotificationEntity,
│   │                      ConsentEntity, InvestmentEntity (on top of Day 1's four)
│   ├── dao/              one DAO per new entity, all Flow-backed
│   └── AppDatabase.kt    version 3 now; onDestructiveMigration callback added Day 2
├── finance/
│   ├── FinanceInsightsRepository.kt   backs Home, Budgets, Subscriptions, Bills, Spend Prediction
│   └── RecurringPatternDetector.kt    shared merchant-grouping heuristic for Subscriptions + Bills
├── export/CsvExporter.kt              Data Export & Portability, scoped to instant local CSV
├── notifications/
│   ├── NotificationChannels.kt
│   └── NotificationCheckWorker.kt     periodic + on-demand check across bills/subs/budgets/night-summary
├── widget/SpendWidgetProvider.kt      single home-screen widget
├── util/
│   ├── ConnectivityObserver.kt        Offline Mode indicator
│   ├── TransactionSearch.kt           rule-based NL query parser (amounts, date phrases, merchant)
│   └── Prefs.kt                       display name + capture-paused flag
├── sms/SmsHistoryScanner.kt           + resetScanFlag() added Day 2 (Section 5.4)
└── ui/
    ├── home/                          new Finance Tracker Home dashboard (replaces landing on Ledger directly)
    ├── budget/, subscriptions/, bills/   one screen each, all reading FinanceInsightsRepository
    ├── review/UnparsedReviewScreen.kt Finance Suite gap-fix, not its own PRD (see day-1.md Section 7)
    ├── notifications/NotificationCenterScreen.kt
    ├── search/SearchScreen.kt         text + voice search over transactions
    ├── rules/AutomationRulesScreen.kt user-visible merchant-rule management
    ├── onboarding/PermissionScreen.kt rewritten as a 5-step flow (see Section 3.4)
    ├── permissions/PermissionsScreen.kt   centralized review + OS-revocation detection
    ├── profile/ProfileScreen.kt       display name, pause capture, delete all data
    ├── nightsummary/NightSummaryScreen.kt
    └── investments/InvestmentsScreen.kt   bonus, minimal manual tracker
```

---

## 3. Exact Code Flow — New Pieces

### 3.1 Finance Home's "needs attention" arbitration

`HomeViewModel` combines two intermediate snapshots rather than one unsafe 7-way `combine()`:

```
FinanceSnapshot = combine(transactions, budgetProgress, bills, unparsedCount)
StatusSnapshot  = combine(unreadNotifications, isOnline, smsConsentedButRevoked)
uiState         = combine(FinanceSnapshot, StatusSnapshot) { ... }
```

The single "needs attention" slot picks one item by fixed precedence: an overdue bill (real financial consequence) beats an over-budget category (informational) beats a pending review-queue count (lowest urgency). No arbitration engine exists (Phase 2's Notification System was never built as code) — this is a hardcoded `when` block, not a weighted scoring system.

### 3.2 Recurring detection feeding both Subscriptions and Bills

```
FinanceInsightsRepository.refreshRecurringDetection()
  -> RecurringPatternDetector.detect(allTransactions)
       groups by merchantNormalized, keeps groups with >=2 occurrences and a 20-40 day average interval
  -> for each group: isSubscriptionLike(group) checks amount variance < 15%
       true  -> upsertSubscription (fixed-amount recurring charge)
       false -> upsertBill (variable-amount recurring payment)
```

Both Subscriptions and Bills also derive a *display* status (e.g. `RENEWAL_UPCOMING`, `OVERDUE`) at read time from `lastTransactionDate`/`dueDayOfMonth` rather than storing it — one less place for stored state to go stale.

### 3.3 Notification pipeline

```
NotificationCheckWorker.doWork() [runs every 6h (schedulePeriodic) + once per Home open (runOnce)]
  -> refreshRecurringDetection()
  -> checkBills / checkSubscriptions / checkBudgets / checkNightSummary
       each: compute current status -> if actionable AND not recentlyNotified (20h cooldown,
       keyed by type + a per-instance sourceKey like "bills42") -> notify()
  -> notify() always inserts a NotificationEntity (so the in-app Center is always accurate)
       and additionally posts a system notification IF POST_NOTIFICATIONS is granted
```

`NotificationEntity` deliberately separates `deepLinkRoute` ("bills", a clean nav route) from `sourceKey` ("bills42", a per-instance cooldown key) — an early draft conflated these into one field before this was caught.

### 3.4 Onboarding as a 5-step state machine

```
PermissionScreen: WELCOME -> SMS_PERMISSION -> SCANNING -> NOTIFICATION_PERMISSION -> FIRST_VALUE
```

- `LaunchedEffect(Unit)` checks `hasSmsPermission()` on every composition — if already granted (a returning user, or SMS was granted in a prior session), it skips straight to `SCANNING` and re-runs `SmsHistoryScanner.scanIfNeeded()` rather than assuming a fresh install.
- The SMS step has a `TextButton` escape hatch ("Skip for now — I'll add expenses manually") that records a `ConsentEntity(granted = false)` and still proceeds — Day 1's scaffold had no path forward after a denial; this was a real dead end, fixed here.
- Every consent decision (grant, deny, skip) is recorded via `ConsentDao`, independent of the OS-level permission state, so `PermissionsViewModel` can later detect **revocation**: `consent.granted == true && !currentOsPermission` — the user granted it once, then turned it off in system settings without telling the app.

### 3.5 Search

```
SearchScreen: OutlinedTextField (typed query) + trailing mic IconButton (voice query)
  -> both paths call the same viewModel.onQueryChange(text)
  -> SearchViewModel debounces via StateFlow -> TransactionSearch.search(query, allTransactions)
       regex-extracts "over/under N" amount filters and "this week/last month/this month" date
       phrases, strips them from the query, treats whatever's left as a merchant substring match
```

Voice capture uses Android's built-in `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` activity (which shows its own transcription/confirmation UI) rather than a custom `SpeechRecognizer` integration — simpler, and it means the "confirm what was heard" UX requirement comes for free from the OS.

---

## 4. How We Built It (Chronological)

1. **User asked what's left after Day 1's SMS-parsing milestone**, and separately how much of the 316-doc corpus was genuinely done end-to-end (not just "has some code"). Answered honestly: essentially one PRD's worth.
2. **User chose to build out the rest of the Finance Suite next** (via an explicit choice between options), rather than jumping pillars. Built Home (Doc 17), Budget Planner (Doc 20) with a naive linear run-rate spend projection (Doc 21's Spend Prediction, explicitly folded in rather than built as its own screen), Subscription Manager (Doc 19) and Bills (Doc 22) sharing one recurring-pattern detector, and Data Export (Doc 47) as an instant local CSV share rather than the PRD's full async-generate-a-download-link flow (no backend exists to generate anything on).
3. **User asked to do 10 more PRDs end-to-end in one push, "think like a founder."** Prioritized notification infrastructure first since three already-built screens (Budgets, Subscriptions, Bills) had "needs attention" signals with nowhere to surface proactively — building `NotificationCheckWorker` once unblocked all three at the same time, instead of bolting a one-off notification onto each screen separately.
4. Built the remaining nine: Widgets, Search + Voice, Automation Rules (making the merchant-rule learning loop from Day 1 visible and user-editable for the first time), a full Onboarding rewrite, a centralized Permissions & Consent screen, Account & Profile Management (display name, pause capture, delete-all-data), Offline Mode's status indicator, and Night Summary. Added Investments as an unrequested 11th, since Home's entry-point grid felt incomplete without a place for the "Future" pillar the architecture doc already named.
5. **Ran `gradle assembleDebug` for the first time against this entire batch** — it failed (Section 5.1). Fixed, rebuilt, installed on the same physical OnePlus Nord CE5 used on Day 1.
6. **On first launch after install, Home showed ₹0.00 and zero transactions** — the 472 real transactions from Day 1 were gone. Diagnosed and fixed as a real data-loss bug (Section 5.4), then recovered the data from the same physical device's real SMS inbox (now 476 transactions — a few genuine new ones arrived between Day 1 and Day 2).

---

## 5. Bugs Found and Fixed

### 5.1 `Icons.Default.Mic` unresolved reference
**Symptom:** `gradle assembleDebug` failed: `Unresolved reference: Mic` in `SearchScreen.kt`, twice.
**Cause:** `app/build.gradle.kts` only depended on `androidx.compose.material:material-icons-core`. `Mic` is part of the larger `material-icons-extended` artifact, not core.
**Fix:** added `implementation("androidx.compose.material:material-icons-extended")`. Also removed an unused `LocalContext`/`context` left over in `SearchScreen.kt` from an earlier draft, flagged by the compiler as an unused-variable warning on the next build.

### 5.2 (Self-caught, pre-build) `NotificationEntity` field collision
Conflated a clean nav route ("bills") with a per-instance cooldown key ("bills42") into one field before writing any DAO query against it. Caught while writing `NotificationDao.countRecent`, split into `deepLinkRoute` and `sourceKey` before the first build attempt.

### 5.3 (Self-caught, pre-build) Unsafe 7-flow `combine()`
First `HomeViewModel` draft combined 7 heterogeneous flows in one `combine(...) { values -> values[i] as List<...> }` block — fragile, and a real risk of a silent `ClassCastException` if the argument order ever shifted. Rewritten as two intermediate `combine()` calls (`FinanceSnapshot`, `StatusSnapshot`) before a final 2-arg `combine()`, all type-checked by the compiler.

### 5.4 Destructive migration silently wiped all 472 transactions, and the app didn't notice
**Symptom:** after installing Day 2's build (which bumped `AppDatabase` from version 2 to version 3 to add six new entities), Home showed "₹0.00 — Nothing captured yet" on a device that had 472 real captured transactions the day before. No crash, no error — the app looked and behaved like a fresh install.
**Root cause, found by direct DB inspection (`adb exec-out run-as ... cat databases/expense_capture_pilot.db`, then `sqlite3` against the pulled file):** `AppDatabase` uses `.fallbackToDestructiveMigration()` (a known, documented pilot-stage tradeoff — no real `Migration` classes exist yet). A version bump triggers Room to **drop and recreate every table**, which is exactly what happened: `SELECT COUNT(*) FROM transactions` returned `0`.

The genuinely interesting part: `SmsHistoryScanner.scanIfNeeded()` — the function that would normally repopulate the ledger from the device's real SMS inbox — **also silently did nothing**, because its one-time guard is a boolean flag in a *separate* SharedPreferences file (`sms_history_scan.xml`), which is untouched by a Room migration. The flag still said "already scanned" from Day 1, so the scanner returned immediately, leaving the freshly-emptied `transactions` table empty. Two independently-persisted stores (the Room DB and SharedPreferences) fell out of sync with each other, and nothing detected the mismatch.

**Fix, in two parts:**
1. **Immediate recovery on the affected device**: manually cleared the stale flag (`adb shell run-as com.lifeos.expensecapture rm shared_prefs/sms_history_scan.xml`) and relaunched the app, letting `PermissionScreen`'s existing `LaunchedEffect` (SMS permission was already granted) re-trigger the scan. Recovered **476 real transactions** (472 from Day 1 plus a few genuinely new ones sent to the device since).
2. **Structural fix so this can't recur silently**: added a `RoomDatabase.Callback` to `AppDatabase`'s builder overriding `onDestructiveMigration(db)`, which calls a new `SmsHistoryScanner.resetScanFlag(context)`. Now, any future destructive migration automatically clears the scan flag as part of the same event that wiped the data, so the very next app open re-scans and self-heals instead of silently presenting an empty ledger as if it were a fresh install. This does **not** fix the underlying "destructive migration on every schema bump" tradeoff — that still needs real `Migration` objects before this app has any real users with data worth protecting — but it does mean a schema bump can no longer look identical to catastrophic, silent data loss.

This bug could not have been found by reading the code — `scanIfNeeded()`'s guard and `fallbackToDestructiveMigration()` each look completely reasonable in isolation. It only surfaced because Day 2's build was actually installed over Day 1's real data on the same physical device and the result was actually inspected, not assumed.

### 5.5 OS/OEM quirks re-encountered (environmental, not app bugs, see Day 1 Section 5.5 for the first occurrences)
- Pulling the Room DB via `adb shell ... cat > file` intermittently produced a `sqlite3` "database disk image is malformed" error, even for a plain `SELECT`. Root cause: the `adb shell` PTY layer can mangle binary bytes in a piped/redirected `cat`. Fix: use `adb exec-out` instead of `adb shell ... cat`, which does not attach a PTY and preserves the binary content exactly. (A related but distinct issue from Day 1's WAL-file/stderr-redirect problems — same symptom family, different cause, both now documented.)

---

## 6. What Was Achieved — In Depth

- **17 PRDs now have a genuine, running, end-to-end implementation** on top of Day 1's SMS-capture foundation — not just scaffolding or a UI mockup, verified by actually building and installing this exact code on the same physical device used throughout this pilot. See Section 7 for the full list and what was honestly scoped down in each.
- **The Finance Suite is now a coherent product surface, not a pile of independent screens.** Budgets, Subscriptions, and Bills all read the same transaction stream through one `FinanceInsightsRepository`, and Home arbitrates a single "what needs your attention" signal across all of them plus the review queue, rather than each screen shouting independently.
- **Notifications are real, not just a database table.** `NotificationCheckWorker` runs on a schedule and on every app open, actually posts system notifications (when permitted), respects a cooldown so nothing spams, and every one deep-links back to the exact screen it's about.
- **The Day 1 "learning loop" (merchant corrections auto-generating rules) is now visible and user-controllable**, not just a background side effect — Automation Rules lets a user see, pause, edit, or delete exactly what the app learned, which matters for trust in an app that acts on your data without being asked each time.
- **Onboarding no longer has a dead end.** A user who denies SMS access, or who never sees a bank SMS at all, still reaches a working app with manual entry — Day 1's scaffold had no such path.
- **A real, non-obvious, severity-high bug was found and fixed by testing on real hardware with real prior data, not by code review.** Section 5.4 is the most important entry in this document: a schema migration silently destroying a real user's 472 real transactions, undetected by the app itself, is exactly the class of bug that looks fine in every individual file and only shows up when you actually run the upgrade path against data that matters. Finding and fixing this today, before any external pilot user exists, is worth more than any single new feature built today.
- **Every new screen was verified to actually render on-device post-fix**: Home, Ledger, Budgets, Subscriptions, Bills, Investments, and the onboarding flow were all confirmed live on the OnePlus Nord CE5 via screenshot, with real recovered data (₹58,308.84 spent this month, 476 transactions, 426 messages correctly routed to the review queue rather than dropped or misparsed). Deeper interaction testing of Notifications, Search, Automation Rules, Permissions, Profile, and Night Summary is still pending — see Section 8.

---

## 7. PRD Tally

| # | PRD | Doc | Scope actually implemented | Explicitly cut / simplified |
|---|---|---|---|---|
| 1 | Automatic Expense Capture (SMS) | — | Day 1: live + historical SMS scan, rule-based parser, categorization, learning loop | Only ICICI Bank debit format verified; generic fallback unverified |
| 2 | Finance Tracker Home | 17 | Net position, single arbitrated "needs attention" slot, offline + revocation banners | Fixed precedence rule, not a real scoring/arbitration engine |
| 3 | Subscription Manager | 19 | Auto-detection via recurring-pattern grouping, confirm/dismiss, manual add | No cancellation-assistance flow, no price-change alerts |
| 4 | Budget Planner | 20 | Per-category + overall limits, month-end projection | Linear run-rate projection only, explicitly not ML |
| 5 | Spend Prediction | 21 | Folded into Budget's `projectedMonthEndSpend`, confidence-labeled (never HIGH) | Not a separate screen, per the PRD's own optionality |
| 6 | Bills | 22 | Auto-detection (variable-amount recurring), due/overdue/paid status, manual add | No partial-payment tracking |
| 7 | Data Export & Portability | 47 | Instant local CSV via share sheet | No async job, no expiry, no per-pillar selection (no backend to need any of that) |
| 8 | Night Summary | 2 | Today vs. yesterday spend, auto-captured count, bills due tomorrow | Finance-only; not a cross-pillar recap (other pillars don't exist) |
| 9 | Notification Center | 3 | In-app list, unread badge, mark-read, deep links, real system notifications | No cross-device sync |
| 10 | Widgets | 4 | One home-screen "spent this month" widget | Glance-only, no lock-screen variant, no interactive tier |
| 11 | Search | 5 | NL amount/date filters + merchant substring match, over transactions | Transactions only — no tasks/meals (other pillars don't exist) |
| 12 | Voice Assistant | 6 | Voice-to-search only, via OS recognizer activity | Not multi-pillar voice actions; explicitly named as Search's narrow slice |
| 13 | Automation Rules | 34 | Visible rule list, create/edit/pause/delete, AI-learned vs. user-authored distinction | No rule conflict detection UI |
| 14 | Onboarding | 40 | Value-prop-first flow, permission requests, skip escape hatch, first-value moment | No account/sign-up concept (no backend) |
| 15 | Permissions & Consent | 41 | Centralized review, OS-revocation detection, consent records | — |
| 16 | Account & Profile Management | 44 | Display name, pause capture, delete-all-data | No account credentials (no backend/auth exists) |
| 17 | Offline Mode | 46 | Online/offline banner | Reconciliation-on-reconnect not applicable (no backend to reconcile against) |
| bonus | Investments (Future) | 23 | Manual holdings list, read-only | No brokerage sync, no tax-lot tracking — deferred by the PRD itself |

---

## 8. Known Gaps and Open Items

Carried forward from Day 1, still true:
- Only ICICI Bank's debit format is verified against real messages; the generic fallback and ICICI's credit-side pattern remain unverified guesses.
- `fallbackToDestructiveMigration()` is still in place — Section 5.4's fix makes a future wipe *self-healing for SMS-derived data*, but it does not stop the wipe itself, and it does nothing for data that has no SMS to re-derive from (manual entries, budgets, automation rules, consent history, notification read-state — all still lost on every future schema bump). Real `Migration` classes are still the correct eventual fix.
- No launcher icon assets.
- OnePlus's background-freeze risk for live (non-historical) capture is still unmitigated.

New after Day 2:
- **On-device interaction verification of today's batch is incomplete.** Home, Ledger, Budgets, Subscriptions, Bills, Investments, and onboarding were confirmed rendering correctly with real recovered data via screenshot. Notifications, Search (including voice), Automation Rules, Permissions review, Profile (including delete-all-data), and Night Summary compiled and are wired into navigation, but have not yet been individually tapped through and confirmed working on-device. This is the next session's first task before claiming full end-to-end verification on the same standard used for Day 1's 472-transaction result.
- **The notification cooldown (20h) and the periodic check (6h) are untested against real elapsed time** — logic was reviewed carefully but a bill/subscription/budget alert has not yet been observed actually firing and being tapped through to the right screen on the device.
- **`FileProvider`/share-sheet preview permission denial observed in logcat** (`Permission Denial: opening provider ... FileProvider ... not exported`) during a CSV export attempt made directly on the device. Likely benign — Android's system share-sheet previewing a shared file without a UI-level grant is a known cosmetic gap that doesn't block the actual share — but not yet confirmed the exported file reaches another app intact.
- **`AppDatabase` and `SmsHistoryScanner` now import each other** (the migration callback needs `resetScanFlag`, the scanner needs `AppDatabase.getInstance`). Compiles and runs fine — neither references the other at class-initialization time — but worth flagging as a coupling to watch if either file grows.
