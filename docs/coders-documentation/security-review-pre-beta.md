# Security & Privacy Review — Pre-Beta Hardening Pass

**Scope:** Priority 3 of the staff-engineer beta-readiness pass. This is a review, not a
redesign — findings are graded by severity, and only the one genuine bug found gets a code
change. Everything else is a recommendation for an explicit future decision, consistent with
"do not modify production behavior unless a bug is discovered."

**Method:** direct inspection of the manifest, every `getSharedPreferences`/Room/network call
site, and the actual data this app has captured on the test device this session (verified via
`adb run-as` pulls, the same method used throughout this project's engineering log).

---

## 1. Summary

The "100% on-device, no backend" claim this project has made since Day 1 holds up under actual
code inspection, not just documentation: **the only network calls anywhere in the codebase are
the two in `UpdateChecker.kt`** (fetching a public update manifest and downloading the update
APK from a GitHub raw-content URL the developer controls). Retrofit is a listed but unused
dependency. No transaction, SMS, task, habit, or goal data has ever left this device.

No critical or high-severity vulnerability was found. Two real, if narrow, gaps are worth
deliberate decisions before a wider beta; one genuine oversight is fixed as part of this pass.

## 2. Findings

### 2.1 — Fixed this pass: crash/log data had nowhere safe to go before now
**Severity: Medium (process gap, not a live vulnerability at time of writing)**

Before Priority 2 of this same hardening pass, there was no crash reporting or structured
logging anywhere in the app — zero `Log.*` calls existed. That's not itself a vulnerability,
but it meant the *only* way to diagnose a failure was pulling the raw on-device database over
ADB, which is a far coarser and more exposed action than a scoped, purpose-built export. Fixed:
`AppLogger`/`CrashHandler`/`CrashLogEntity` now capture exceptions locally, and `AppLogger.e()`
call sites were reviewed for accidental PII (see 2.2) as they were written.

### 2.2 — Reviewed, no leak found: logged context strings
**Severity: N/A (verified clean)**

Every new `AppLogger.e()` call site added in this pass was audited for what it actually logs:
- `NotificationCheckWorker`: only the check's own name (e.g. `"checkBills"`) and the exception -
  never a transaction amount, merchant name, or task/habit title.
- `ParseIncomingSmsWorker`: the SMS *sender* (e.g. `"AD-ICICIT-S"`) is logged on an ingest
  failure. This is a bank's short institutional gateway code, not a personal phone number or
  message body - judged acceptable context for debugging a parse failure, not a PII leak.
- Exception `.message`/stack traces: Kotlin/Java stack traces don't embed variable *values*
  (only class/method/line), and this codebase's own `error(...)` calls are static strings (e.g.
  `"Uncategorized category missing"`) - checked, none interpolate live financial data.

### 2.3 — Accepted tradeoff, not a bug: no field/file-level database encryption
**Severity: Low-Medium — recommend a decision before a public beta, not before the 2-person test**

The Room database is not wrapped in SQLCipher or any app-level encryption. In practice, the
data is still encrypted at rest by Android's own file-based encryption (standard since Android
7+) while the device is locked, and is further protected by `android:allowBackup="false"`
(already set - this alone blocks the most common extraction path, ADB/cloud backup). The
realistic exposure is: a **rooted device**, or a **debuggable build installed via ADB with USB
debugging enabled** (which is exactly how this project's own on-device verification has worked
all session - `adb run-as` reading the plaintext database is the literal method used to verify
every migration in this codebase). That is a meaningfully narrower threat model than "anyone who
finds the phone," but it is real, and it's the honest reason this session's verification
process could pull the database so easily.
**Recommendation, not done here:** SQLCipher (or Room's own `androidx.sqlite` encryption
support) before scaling past a couple of testers on debug builds - deliberately not implemented
in this pass since it needs a real key-management decision (Android Keystore-backed passphrase)
that's a bigger architectural change than "add a dependency."

### 2.4 — Accepted tradeoff: the home-screen widget shows a real number in plaintext
**Severity: Low**

`SpendWidgetProvider` displays "Spent this month: ₹X" directly on the home screen - visible to
anyone glancing at an unlocked phone, a real (if minor) shoulder-surfing exposure for financial
data. The widget's own kdoc already shows this was considered: a lock-screen variant was
explicitly scoped out for exactly this reason. The home-screen case itself was accepted as the
PRD's "single-pillar summary" type. No change made - this is a product-level call (what a
widget is *for*), not something to alter unilaterally in a hardening pass.

### 2.5 — Verified correct: permission footprint matches implementation exactly
**Severity: N/A (verified clean)**

`RECEIVE_SMS`/`READ_SMS`/`POST_NOTIFICATIONS`/`ACCESS_NETWORK_STATE`/`REQUEST_INSTALL_PACKAGES`/
`INTERNET` - every one is used by a real, implemented feature; none are unused/speculative.
`SmsReceiver` is `exported="true"` (required to receive the system SMS broadcast) but gated by
`android:permission="android.permission.BROADCAST_SMS"`, a system-only permission - no
third-party app can spoof a fake SMS broadcast into this receiver. The `FileProvider` is
correctly `exported="false"` with `file_paths.xml` scoped to only two cache subdirectories
(`exports/`, `updates/`) - it cannot serve the Room database file or any other app-private data,
even in principle.

### 2.6 — Verified correct: the in-app updater's real safety net
**Severity: N/A (verified clean, context for a known scope note)**

The update mechanism has no checksum/signature pinning of its own on the downloaded APK - noted
as an accepted gap in this project's own engineering log already. Worth stating explicitly here:
**Android's package installer itself refuses to install an "update" unless it's signed with the
same signing key as the currently-installed app**, regardless of what this app's own code does
or doesn't verify. That OS-level check is the actual backstop, and it's not bypassable from
application code - a tampered or substituted APK at the download URL would fail to install, not
silently replace the app with attacker-controlled code.

### 2.7 — No secrets in code
**Severity: N/A (verified clean)**

No API keys, tokens, or credentials of any kind exist anywhere in the source tree - consistent
with there being no third-party service this app talks to yet (matching the "no AI key
anywhere" conversation earlier this session).

### 2.8 — Not addressed in this pass: release build hardening
**Severity: Low at current pilot scale; relevant before a Play Store submission**

`isMinifyEnabled = false` for release builds - no R8/ProGuard shrinking or obfuscation. Fine for
a debug-signed pilot with two testers; worth revisiting alongside a real release-signing
pipeline (already tracked in `technical-roadmap.md`) rather than as a security fix on its own.

## 3. What changed as a direct result of this review

Nothing beyond what Priority 2 already shipped (crash/log infrastructure, reviewed for PII as it
was written). Every other item above is a recommendation for a future, explicit decision, not a
silent change to production behavior.
