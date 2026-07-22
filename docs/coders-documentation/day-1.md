# Coder's Documentation — Day 1

**Scope of Day 1:** taking the pilot from a design doc (`docs/mvp-pilot/01-system-design-and-architecture.md`) to a real, working Android app that automatically captured 472 real transactions from a live device's actual SMS history. This log covers the tech stack, the exact code flow, how it was built step by step, every bug hit and fixed, and what's actually proven to work versus what's still open.

This folder is a running engineering log, one file per day. Read this file top to bottom for the full story, or jump to a section using the index below.

- [1. Tech Stack](#1-tech-stack)
- [2. Environment Setup](#2-environment-setup)
- [3. Exact Code Flow](#3-exact-code-flow)
- [4. How We Built It (Chronological)](#4-how-we-built-it-chronological)
- [5. Bugs Found and Fixed](#5-bugs-found-and-fixed)
- [6. What Was Achieved — In Depth](#6-what-was-achieved--in-depth)
- [7. Known Gaps and Open Items](#7-known-gaps-and-open-items)

---

## 1. Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin | Native Android, required for reliable `SmsManager`/`BroadcastReceiver` access — cross-platform frameworks (React Native/Flutter) buy nothing here since the core capability is native-only anyway |
| UI | Jetpack Compose (Material 3) | Modern declarative Android UI toolkit; no XML layouts |
| Local persistence | Room (SQLite) | Offline-first — the entire capture/ledger loop works with zero network connectivity |
| Async | Kotlin Coroutines + Flow | Used throughout — Room DAOs return `Flow`, UI state derives via `StateFlow`, background work uses `suspend` functions |
| Background SMS processing | `BroadcastReceiver` + `WorkManager` (`CoroutineWorker`) | The receiver's `onReceive` has a very short execution window and Room access needs a background thread, so it just enqueues work rather than parsing inline |
| Navigation | Jetpack Navigation Compose | Two-destination graph: `permission` → `ledger` |
| Build system | Gradle 8.7, Android Gradle Plugin 8.5.0, Kotlin 1.9.24, KSP 1.9.24-1.0.20 | KSP (not KAPT) for Room's annotation processing — faster, and the modern recommended path |
| Target platform | `minSdk 26`, `compileSdk`/`targetSdk 34` | Android 8.0+ |
| Planned but not yet wired | Retrofit + Gson (backend sync), FastAPI/Postgres backend (see architecture doc) | Dependencies are declared in `app/build.gradle.kts` for the next increment; no networking code exists yet — the app is 100% local-only right now |

No ML anywhere in the capture path — SMS parsing is a deliberately simple rule-based regex engine (see Section 3). This was a design decision in the architecture doc, not a shortcut: bank/UPI SMS formats are regular enough per-bank that rules get you very far, and it keeps 100% of parsing on-device (raw SMS text never transmitted anywhere, ever).

---

## 2. Environment Setup

None of this existed on the dev machine before Day 1. Everything below was installed and configured from scratch:

1. **Java 17** — already present on the machine, but `JAVA_HOME` was unset. Worse: `java` on `PATH` resolved through `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`, which is an Oracle PATH-management shim, **not** the real JDK — it has no `javac`, `jlink`, etc. `JAVA_HOME` was pointed at the real install instead: `C:\Program Files\Java\jdk-17`. This one is worth remembering: `(Get-Command java).Source` on a Windows box with Oracle's installer can silently lie about where the JDK actually is.
2. **Android SDK** — installed via the standalone "command-line tools only" package (not full Android Studio, since that wasn't requested), to `%LOCALAPPDATA%\Android\Sdk`. Every download was verified against its published SHA-256 checksum before use, and one initial download attempt (via `Invoke-WebRequest` with the default progress-bar renderer) silently truncated a 155 MB file — PowerShell's progress-bar rendering is a known cause of corrupted large downloads; the fix is `$ProgressPreference = 'SilentlyContinue'` before the request.
3. **SDK packages installed**: `platform-tools`, `platforms;android-34`, `build-tools;34.0.0` — matches the project's `compileSdk`/`targetSdk`. All 7 SDK licenses accepted (non-interactively, via `cmd /c "sdkmanager --licenses < yes_input.txt"` — a plain PowerShell `|` pipe into the `.bat` → Java process chain did not forward stdin correctly, `cmd`'s `<` redirection did).
4. **Gradle 8.7** — no system Gradle existed and the project's own Gradle wrapper JAR (a binary file) can't be produced by a text-based tool, so a real Gradle distribution was downloaded and added to `PATH` instead, to actually compile and verify the code rather than trust it by eye.
5. **Project-level config**: `android-app/local.properties` (gitignored, machine-specific) with `sdk.dir` pointing at the installed SDK — the reliable way for Gradle to find the SDK without depending on a shell restart to pick up the env var.

Result: `gradle assembleDebug` produces a real, installable debug APK from a machine that had none of this tooling at the start of the day.

---

## 3. Exact Code Flow

### 3.1 Package layout

```
com.lifeos.expensecapture/
├── App.kt                          Application class - seeds default categories on first run
├── MainActivity.kt                 Hosts the Compose UI
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt          Room database, 4 entities, type converters
│   │   ├── entity/                 TransactionEntity, CategoryEntity, MerchantRuleEntity, CorrectionEntity
│   │   └── dao/                    One DAO per entity
│   ├── repository/TransactionRepository.kt   The only class the UI layer talks to for data
│   └── seed/DefaultCategories.kt   13 seed categories incl. "Uncategorized"
├── categorization/CategorizationEngine.kt    Rule-based merchant -> category lookup
├── sms/
│   ├── SmsReceiver.kt              BroadcastReceiver for SMS_RECEIVED (live/new messages)
│   ├── SmsHistoryScanner.kt        One-time scan of existing SMS inbox (added Day 1, see Section 5)
│   ├── ParseIncomingSmsWorker.kt   CoroutineWorker - the live-message capture path
│   ├── TransactionIngestor.kt      Shared parse -> categorize -> insert pipeline (added Day 1)
│   └── parser/
│       ├── BankTemplate.kt         Per-bank regex templates (iciciBank, genericTransactionAlert)
│       ├── TransactionParser.kt    The layered rule engine that picks a template and extracts fields
│       └── ParseResult.kt          Sealed class: Parsed | Unparsed
└── ui/
    ├── theme/                      Material3 theme, color, typography
    ├── onboarding/PermissionScreen.kt   Consent screen + permission request + triggers history scan
    ├── ledger/
    │   ├── LedgerScreen.kt         Main list view + FAB for manual entry
    │   ├── LedgerViewModel.kt      Combines transactions + categories into UI state
    │   ├── CategorizeSheet.kt      Bottom sheet to recategorize a transaction
    │   └── ManualEntryDialog.kt    Manual transaction entry fallback
    └── navigation/PilotApp.kt      2-destination NavHost: permission -> ledger
```

### 3.2 The two SMS capture paths (both converge on the same pipeline)

**Path A — live/new messages (existed from the first scaffold):**

```
SMS arrives on device
  -> Android delivers SMS_RECEIVED broadcast
  -> SmsReceiver.onReceive() extracts sender + body
  -> enqueues a OneTimeWorkRequest for ParseIncomingSmsWorker via WorkManager
     (not parsed inline - onReceive has a short execution window, and Room needs a background thread)
  -> ParseIncomingSmsWorker.doWork() calls TransactionIngestor.ingest(db, sender, body, now)
```

**Path B — existing inbox history (added Day 1, see Section 5.1 for why):**

```
User grants SMS permission (PermissionScreen)
  -> SmsHistoryScanner.scanIfNeeded(context) runs once (gated by a SharedPreferences flag)
  -> queries Telephony.Sms.Inbox.CONTENT_URI for every existing message, oldest first
  -> for each message, calls the SAME TransactionIngestor.ingest(...)
```

**Shared pipeline (`TransactionIngestor.ingest`), used by both paths:**

```
TransactionParser.parse(sender, body)
  1. Filter BankTemplates.all down to "candidate" templates: sender matches OR body contains
     transaction-ish keywords (debited/credited/upi/a-c/account)
  2. For each candidate template, in order (specific/verified templates listed first):
       try debitPattern.find(body) -> if hit, done
       try creditPattern.find(body) -> if hit, done
  3. No candidate matched at all -> ParseResult.Unparsed (see Section 7 for what this doesn't do yet)
  4. A pattern matched -> ParseResult.Parsed(amount, direction, merchantRaw, confidence, templateName)

  -> CategorizationEngine.categorize(merchantRaw)
       - normalizes merchant name, checks merchant_rules table for a substring match
       - falls back to the "Uncategorized" category if no rule matches
  -> TransactionEntity inserted into Room (source = SMS_AUTO, confidenceScore from the parser)
```

### 3.3 UI data flow

```
LedgerScreen
  -> creates TransactionRepository (wraps all 4 DAOs)
  -> creates LedgerViewModel(repository)
      -> uiState: StateFlow<LedgerUiState> = combine(repository.observeLedger(), repository.observeCategories())
      -> this is why the UI updates automatically the instant SmsReceiver/SmsHistoryScanner insert a
         row - Room's Flow-backed queries push new emissions straight through to Compose's
         collectAsState(), no manual refresh logic anywhere
  -> tapping a transaction opens CategorizeSheet
      -> selecting a category calls viewModel.recategorize()
      -> TransactionRepository.recategorize() does THREE things in one call:
           1. updates the transaction's categoryId
           2. inserts a CorrectionEntity (the audit trail / future ML training signal)
           3. upserts a MerchantRuleEntity so the SAME merchant auto-categorizes correctly next time
      -> this 3-step write is the entire "learning loop" for the pilot
  -> FAB opens ManualEntryDialog for cash spend / anything the parser missed
```

### 3.4 Permission + first-run flow

```
MainActivity -> PilotApp (NavHost) -> starts at "permission" destination
PermissionScreen:
  - LaunchedEffect(Unit): if SMS permission already granted (e.g. re-launch), immediately
    triggers SmsHistoryScanner.scanIfNeeded() then navigates to "ledger"
  - otherwise shows the plain-language consent explanation + "Grant SMS access" button
  - on grant: same scan-then-navigate sequence, with a "Scanning your existing messages..."
    loading state shown during the scan (which can take a few seconds against months of SMS)
```

---

## 4. How We Built It (Chronological)

1. **Design first.** Wrote `docs/mvp-pilot/01-system-design-and-architecture.md` before any code — scope, explicit non-goals, data model, API surface, security minimums, tech stack recommendation. Confirmed Android-only (iOS has no public API for reading SMS content at all — this is a hard platform constraint, not a preference).
2. **Scaffolded the full Android project** from nothing: Gradle files, manifest, Room schema, the SMS receiver/worker, the parser (one generic, unverified template), the Compose UI (permission screen, ledger, categorize sheet, manual entry), and a README documenting known gaps honestly instead of glossing over them.
3. **Configured the dev machine's Android toolchain from scratch** (Section 2) since none of it existed - Java, Android SDK, Gradle, all installed and verified with checksums.
4. **Built for real and found real bugs** — see Section 5.1 and 5.2. The first `gradle assembleDebug` run genuinely failed to compile; fixing it was the point of building at all instead of just eyeballing the code.
5. **Connected a real device** (OnePlus Nord CE5) via `adb`, rather than fighting with Windows Hyper-V/WHPX emulator setup that would've needed admin rights we didn't have. Installed and launched the debug APK.
6. **Discovered the app only listened for NEW SMS, never scanned history** — a real gap between what the architecture doc promised (Section 2's user flow explicitly says "scans existing transaction SMS + listens for new ones") and what the first code pass actually implemented. Built `SmsHistoryScanner` + refactored the shared `TransactionIngestor` to fix it (Section 5.3).
7. **Tested a real live transaction** — made a real payment, watched logcat live, and confirmed it was NOT captured. Diagnosed two independent causes: OnePlus's proprietary process-freezer (`OplusHansManager`) freezing the app ~10 seconds after backgrounding, and the generic parser template not matching the real ICICI Bank SMS format at all.
8. **Pulled the real SMS text via `adb shell content query`** and found the actual structural mismatch: ICICI's format puts "debited" *before* the amount and has no "to/towards/at" marker before the merchant - it's `; MERCHANT credited.` Built a verified `iciciBank` template against the real sample text.
9. **Re-verified against the full real inbox** (uninstall/reinstall to force a clean history re-scan, since OnePlus also blocks `adb shell pm clear` and `adb shell pm grant` with `SecurityException`s not seen on stock Android) — **472 real transactions captured**, clean merchant names.

---

## 5. Bugs Found and Fixed

### 5.1 `weight()` import resolving to an internal symbol
**Symptom:** `gradle assembleDebug` failed with `Cannot access 'weight': it is internal in 'androidx.compose.foundation.layout'` in `LedgerScreen.kt`.
**Cause:** an explicit `import androidx.compose.foundation.layout.weight` resolved ambiguously against an internal-only symbol in the installed Compose BOM version, instead of the intended `RowScope`/`ColumnScope` extension.
**Fix:** switched to the standard `import androidx.compose.foundation.layout.*` wildcard that real Compose codebases use, which resolves `weight` correctly per-receiver at the call site.

### 5.2 Missing `@OptIn(ExperimentalMaterial3Api::class)`
**Symptom:** same build failure, second error: `TopAppBar` is an experimental Material3 API.
**Cause:** the opt-in was added to `CategorizeSheet` (which uses `ModalBottomSheet`, also experimental) but missed on `LedgerScreen`.
**Fix:** added the annotation to `LedgerScreen`.

### 5.3 No SMS history scan — only new messages were ever captured
**Symptom:** user asked "if I do an expense now, would it be tracked" and separately "can it not read from sms" after finding old transactions absent from the ledger.
**Cause:** `SmsReceiver` only ever handled the live `SMS_RECEIVED` broadcast. Nothing scanned the SMS inbox for messages that existed before install, even though the architecture doc's user flow explicitly called for it.
**Fix:** added `SmsHistoryScanner` (queries `Telephony.Sms.Inbox.CONTENT_URI`, gated by a one-time SharedPreferences flag) and refactored the parse/categorize/insert logic out of `ParseIncomingSmsWorker` into a shared `TransactionIngestor` object so the live path and the history-scan path can never drift apart. Wired the scan to run right after permission grant, with a loading indicator.

### 5.4 Generic SMS template didn't match real ICICI Bank format
**Symptom:** after fixing 5.3, a real live transaction still didn't appear. Pulling the raw SMS via `adb shell content query --uri content://sms/inbox` showed the real message text didn't match the shipped generic regex at all.
**Cause:** the one template shipped in the original scaffold was explicitly labeled as an unverified starting guess (per the architecture doc's own instruction: "collect real sample SMS... don't fabricate specific per-bank regexes without real messages to verify against"). The real ICICI format puts "debited" before the amount (`debited for Rs 1.00`, not `Rs 1.00 debited`) and marks the merchant with `; MERCHANT credited.` instead of `to/towards/at MERCHANT`.
**Fix:** added a verified `iciciBank` template built directly against 5 real captured messages, ordered before the generic fallback in `BankTemplates.all` so bank-specific matches win. The credit-side pattern for ICICI is explicitly commented as **unverified** (no real incoming-credit SMS sample was available yet) rather than presented as equally trustworthy.

### 5.5 OnePlus-specific `adb` restrictions (environmental, not app bugs)
Encountered and worked around during testing, worth recording so the next session doesn't waste time rediscovering them:
- `adb shell pm clear <pkg>` → `SecurityException` (no `CLEAR_APP_USER_DATA` for shell on this device/OxygenOS build). Worked around with uninstall + reinstall instead.
- `adb shell pm grant <pkg> <permission>` → `SecurityException` (no `GRANT_RUNTIME_PERMISSIONS` for shell). Worked around by granting through the app's own UI instead - which is the realistic pilot-user path anyway.
- Fresh (non-update) `adb install` hung until the user manually confirmed an on-device install prompt - not something adjustable from the host side.

---

## 6. What Was Achieved — In Depth

By the end of Day 1, the following is **proven**, not just written:

- **A real Android app was designed, built, and compiled from zero tooling on the dev machine to a working debug APK** — including standing up the entire Android SDK/Gradle/JDK toolchain from scratch and catching real compiler errors (Section 5.1-5.2) that a code-review-only pass would have missed entirely.
- **Installed and run on a real physical device** (OnePlus Nord CE5, `CPH2717`), not an emulator — deliberately chosen because the entire point of this pilot is real SMS parsing, which an emulator can only simulate.
- **The offline-first local data pipeline works end-to-end**: SMS → on-device regex parse → merchant categorization → Room database → Compose UI, with zero network dependency anywhere in that path, verified by direct SQLite inspection on the device (via `adb exec-out run-as ... cat` + WAL-file-aware reconnection, since Room uses WAL journal mode by default).
- **The "learning loop" is wired and functional**: recategorizing a transaction writes a `MerchantRuleEntity`, so the exact seed dataset for a future ML categorization model (per the Phase 5 AI/ML architecture docs) is already being generated by real user corrections, not hypothetically.
- **472 real transactions were captured automatically from actual bank SMS history**, with recognizable merchant names (Blinkit, ZOMATO, Shadowfax, AstroSage AI, and others) extracted correctly by a hand-built regex template verified against real message text — this is the pilot's core hypothesis (automatic capture removes enough friction to be worth using) getting its first piece of real evidence, on day one, on real data.
- **A genuine, verified bank-specific SMS template exists for ICICI Bank**, built the way the architecture doc always intended: from real sample messages, not speculation, with the parts that are still guesses (the credit-side pattern) honestly labeled as such rather than presented with false confidence.
- **Three real, non-obvious environment/OS behaviors were discovered and documented** rather than silently worked around and forgotten: OnePlus's process-freezer interfering with background broadcast delivery, and two separate `adb` shell permission restrictions specific to this OEM's build.
- **Every fix was committed with a clear, honest message describing what broke and why** — the git history for this pilot is itself a readable record of what was learned, not just what was shipped.

---

## 7. Known Gaps and Open Items

Carried over from the original scaffold, still open after Day 1:

- **Unparsed-message review queue is still not wired.** `ParseResult.Unparsed` messages are silently discarded rather than surfaced for manual confirmation. This means a parse failure and "no transaction happened" currently look identical to the user - a real risk once beyond a single verified bank template. This should be the next fix before adding more bank templates.
- **Backend sync does not exist yet.** The app is 100% local-only. Retrofit is a declared dependency; no `ApiService` or sync repository has been written.
- **Weekly summary push notification is not implemented**, and `POST_NOTIFICATIONS` is deliberately not yet in the manifest (permission footprint should match implemented functionality, not get ahead of it).
- **Only one bank template is verified** (ICICI Bank debit). The generic fallback template for other banks remains an unverified guess - collect real sample SMS from any other banks before trusting it.
- **ICICI's credit-side pattern is unverified** - no real incoming-credit SMS has been captured yet to check it against.
- **No launcher icon assets.** Needed before distributing to any pilot tester beyond this dev device.
- **The OnePlus background-freeze risk for live (non-historical) capture is identified but not yet mitigated.** Worth investigating whether disabling battery optimization / enabling auto-launch for the app in OnePlus's settings resolves it, and whether that's something worth asking pilot users to do or a sign the live-capture path needs a different approach (e.g., periodic re-scan instead of relying solely on the broadcast).
- **Merchant name extraction is functional but not clean everywhere** - names like "sohomjana2 2 ok" (a UPI ID artifact) show the extraction sometimes needs trimming logic beyond a raw regex capture group. Real-world example, real-world imperfection, worth improving before this scales beyond a single test device.
