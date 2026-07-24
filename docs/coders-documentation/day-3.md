# Coder's Documentation — Day 3

**Scope of Day 3:** a second real bank template (SBI), built the same rigorous way as ICICI - from a real sample, verified by a real unit test, not a guess. That work led to finding and fixing two more real, previously-undiscovered production bugs (a permanently-stuck SMS scan, and duplicate-row bloat in two tables), both confirmed on a real device with a real pilot tester's data. On top of that: a full design-system pass grounding the app's visuals in the Phase 7 Design System docs instead of Compose defaults, one more PRD (Morning Dashboard, scoped to Finance-only), a real launcher icon, and the project's first automated tests.

- [1. New Bank Template: SBI](#1-new-bank-template-sbi)
- [2. Two More Real Bugs Found and Fixed](#2-two-more-real-bugs-found-and-fixed)
- [3. Automated Tests](#3-automated-tests)
- [4. Design System Pass](#4-design-system-pass)
- [5. Morning Dashboard (Doc 01)](#5-morning-dashboard-doc-01)
- [6. Launcher Icon and Tester Distribution](#6-launcher-icon-and-tester-distribution)
- [7. Environment Notes](#7-environment-notes)
- [8. PRD Tally](#8-prd-tally)
- [9. Known Gaps and Open Items](#9-known-gaps-and-open-items)

---

## 1. New Bank Template: SBI

A pilot tester (Sohom) reported his ledger wasn't updating; his account is SBI, not ICICI. Following the same rule that the whole parsing system is built on - never fabricate a bank-specific regex without a real sample - a real SBI UPI-debit SMS was collected first:

```
Dear UPI user A/C X5359 debited by 1.00 on date 25Jul26 trf to harshgovil460@ok Refno
620647267681 If not u? call-1800111109 for other services-18001234-SBI
```

Two things about this format that mattered, found only by matching against the real text: there's no "Rs"/"INR" prefix before the amount at all (`debited by 1.00`, not `debited for Rs 1.00`), and the "merchant" SBI's alert gives you is a raw UPI VPA (`harshgovil460@ok`), not a resolved name the way ICICI shows `Blinkit`. The new `sbiBank` template in `BankTemplate.kt` is verified against exactly this sample; its credit-side pattern is an unverified mirror-image guess, flagged the same way ICICI's was until a real incoming-credit sample turned up (see Section 2).

The `senderPatterns` for SBI are also an unverified guess (only the message body was available, not the actual sender ID) - this doesn't block correct parsing today, since `TransactionParser`'s candidate filter also matches on body keywords regardless of sender.

**A second real find, along the way:** while investigating a test message, a genuine real incoming ICICI credit SMS turned up on the same device (`"...is credited with Rs 1.00... from Sohom Jana..."`) - the exact class of message ICICI's credit pattern had never been tested against. It matched correctly on the first try. That pattern's doc comment and the credit-side test case were updated from "unverified" to confirmed.

---

## 2. Two More Real Bugs Found and Fixed

### 2.1 A permanently-stuck SMS scan (the tester-reported bug)

**Symptom:** Sohom's ledger stopped updating after a specific date and never recovered; Home showed ₹0.00 for the current month because there was, literally, no data captured for the current month.

**Root cause:** `SmsHistoryScanner` used a single one-time boolean flag ("has this device ever been fully scanned"), set only after the *entire* SMS inbox had been processed. Reproduced identically on the dev device: scanning 910+ real messages, the scan stopped partway through - most likely the OS suspending the app mid-scan, the same background-reliability class of risk flagged for live capture since Day 1 - and the flag still ended up marked "done." Every later app open saw "already scanned" and never looked again. The user was stuck wherever the interruption happened to land, permanently.

**Fix:** replaced the single flag with a `last_scanned_date` watermark, persisted after *every message*, not once at the end. An interruption now only costs whatever wasn't processed since the last watermark write - the next call resumes from there instead of either being stuck forever or re-scanning everything. Also wired the same catch-up scan into `NotificationCheckWorker` (already running periodically and once per Home open) as a safety net independent of the user manually reopening the app, and added an app-wide `ON_RESUME` lifecycle hook (`PilotApp.kt`) so simply switching away and back - not just a full force-close - reliably retries too. All three trigger paths were verified on-device: a rolled-back watermark reached the true latest message in the inbox via a plain background→foreground cycle alone, with no force-stop involved.

### 2.2 Duplicate rows from repeated scanning (found immediately after fixing 2.1)

**Symptom:** making the scan retry far more often (Section 2.1's fix) immediately exposed a second, older bug: `unparsed_messages` jumped from 1266 to 2135 rows within minutes on the same device, and an earlier version-bump migration had already produced 923 transaction rows where 472 real ones should have existed.

**Root cause:** both `TransactionDao.insert` and `UnparsedMessageDao.insert` were already written with `OnConflictStrategy.IGNORE` - but neither table had a unique constraint for that strategy to actually act on. Every re-scan of an already-seen message inserted a fresh duplicate row with a new auto-generated ID, since nothing there conflicted with anything.

**Fix:** added a `sourceHash` column (`sender::body`) with a unique index to both `TransactionEntity` and `UnparsedMessageEntity`, set from the real SMS sender/body in `TransactionIngestor`. Manual entries get a random UUID instead, since two genuinely identical manual entries shouldn't be deduplicated against each other. Verified on-device: after the schema migration and a fresh full re-scan, both tables show zero duplicate `sourceHash` groups, and the unparsed count dropped from an inflated 2135 to a real, stable 273-274.

These two bugs compounded each other in an interesting way: fixing the first (making scans retry aggressively) is what turned the second (silent duplicate accumulation) from a slow leak into something that ballooned within minutes. Neither would have been found without a real tester's real, larger-than-the-dev-device SMS history hitting the first bug in the first place.

---

## 3. Automated Tests

The project's first automated tests: `TransactionParserTest.kt`, a plain JVM unit test (no Android framework dependency needed, since `TransactionParser`/`BankTemplate`/`ParseResult` are pure Kotlin) locking in behavior against the real captured samples from Day 1, Day 3, and the newly-found ICICI credit message:

- Real ICICI debit SMS parses correctly.
- Real ICICI credit SMS parses correctly (newly verified, see Section 1).
- Real SBI UPI debit SMS parses correctly.
- An unrelated personal SMS is not misparsed as a transaction.

Run via `gradle testDebugUnitTest` - all four pass. The point of this file is narrow but real: a future regex edit that silently breaks an already-working format (exactly what caused Day 1's original bug) now fails a fast test instead of waiting for another live device failure to surface it.

---

## 4. Design System Pass

The app had a nominal brand color (`Primary = 0xFF2E6F58`, a calm teal-green) since the very first scaffold, but only `primary`/`secondary`/`background` were ever set - every other surface (cards, containers, errors) fell back to Compose's untouched Material3 baseline, which is why screenshots looked like generic lavender/purple defaults despite a teal primary existing in the code.

The Phase 7 Design System docs (`docs/phase-7-design-system-ux/`) were read for concrete tokens to apply. They turned out to be specification *outlines* - Purpose/Sections/Deliverables for docs that were scoped but never filled in with actual hex values or type scales; every "Deliverable" explicitly says things like "not final hex values." So there was nothing literal to copy. What they do specify clearly, and what this pass implements:

- **"Calm authority, quiet competence"** as the visual identity (Doc 02) - realized as a deep teal-green primary, warm off-white surfaces instead of stark white, and softened 16dp card corners (Doc 08 describes card anatomy/density but no radius value - 16dp is Compose's own Material3 default for "medium," now applied consistently everywhere via `Theme.kt` instead of per-screen).
- **"Encourage, never guilt" semantic color** (Doc 04) - the concrete bug this caught: `BudgetScreen.kt`'s over-budget progress bar used `MaterialTheme.colorScheme.error` (literal alarm-red) for "you spent more than planned this month." Replaced with a two-tier muted amber (`Warning`/`WarningStrong` in `Color.kt`) so "over budget" and "the app is broken" never look the same. Real errors (SMS permission actually revoked, breaking real functionality) still correctly use the real `error` role - that distinction was already right elsewhere and wasn't touched.
- **Tabular numerals for currency** (Doc 03's one concretely actionable rule for a finance app) - `AmountLarge`/`AmountBody` text styles (`fontFeatureSettings = "tnum"`) applied to Home's spend total, Budget's progress line, and Ledger's transaction rows, so amounts stay aligned instead of shifting width per-digit.
- A debit/credit color distinction in the Ledger (neutral for money out, primary-teal for money in) - a standard, useful finance-app pattern, not from the docs directly but consistent with their intent.
- Two small correctness fixes found while doing this: `HomeScreen.kt`'s entry-point subtitles used hardcoded `Color.Gray` (wouldn't adapt to dark mode); now uses `MaterialTheme.colorScheme.onSurfaceVariant`.

Verified on-device: Home, Budgets, and Ledger all render with the new palette, rounded cards, and tabular amounts with no crashes.

---

## 5. Morning Dashboard (Doc 01)

The last remaining PRD in the Daily Experience Core Surfaces group. The full spec (`01-morning-dashboard-prd.md`) calls for cross-pillar candidate-item ranking, a tie-break engine, a Proactivity Ladder, a Context Engine, and a memory-recall service for "why this, why now" justification - none of which exist, and none of which make sense to build for one rule-based Finance pillar with no ML anywhere in the stack.

What's implemented is the one testable idea from the PRD's own Success Criteria that still applies at this scope: `MorningBriefingViewModel` shows a dismissible card once per calendar day on first Home open, leading with the single most relevant thing using the same precedence Home's own attention logic already uses (overdue bill beats due-today beats over-budget), plus a one-line "yesterday you spent ₹X" for context. On a genuinely quiet day it says so explicitly ("Nothing needs your attention this morning") rather than padding with manufactured content - the PRD's own "low-content composition rule."

One real bug caught before it shipped: the first draft tried to mark the card "shown today" inside the ViewModel's `init` block by reading `uiState.value` - but `uiState` is a `stateIn(..., SharingStarted.WhileSubscribed(5000), ...)` flow, so its value at `init` time is still the default (`visible = false`), not the real computed result, since nothing has subscribed yet. Simplified to only persist "shown today" on explicit dismiss instead.

Verified on-device: the card rendered with real content on first load (visible in an early screenshot before being dismissed); not separately re-tested for the "next calendar day" transition, since that requires actually waiting for or faking a day boundary.

---

## 6. Launcher Icon and Tester Distribution

The app had no launcher icon at all (a known Day 1 gap, "needed before distributing to any pilot tester"). Since it's now actually going to a tester, this was fixed: a simple purple bar-chart mark as an adaptive icon (`mipmap-anydpi-v26` only, since `minSdk 26` is exactly when adaptive icons were introduced - no legacy fallback needed). Verified on the real device via the App Info screen: renders as a clean rounded purple icon with the white bars, not a broken resource or the generic default.

Also hit, while getting a build onto the tester's Samsung phone: Samsung's Auto Blocker and Google Play Protect both independently block sideloaded APKs requesting SMS permissions by default on recent Android/One UI versions - not a bug, a real distribution friction point worth remembering for the next tester. Separately, on the dev device, `adb install` started failing with `INSTALL_FAILED_VERIFICATION_FAILURE` mid-session - fixed via `adb shell settings put global verifier_verify_adb_installs 0`, which disables Android's package-verifier service specifically for adb-driven installs.

---

## 7. Environment Notes

- `adb shell content insert --uri content://sms/inbox ...` (to fake a test SMS for on-device verification) failed with `Permission denied` on this OxygenOS build - another OEM-specific restriction in the same family as Day 1's `pm clear`/`pm grant` blocks. Worked around by verifying the SBI parser via the JVM unit test instead (Section 3), which is arguably the more correct verification method anyway - it exercises the real compiled Kotlin regex engine directly, with zero device/OS variability.
- Reliable automated on-device UI testing assumes exclusive control of the device. Several verification attempts this session were interrupted by the physical device owner using the phone concurrently (WhatsApp, LinkedIn, a lock-screen) - not an app bug, but worth noting as a real constraint on how much can be automated against a shared personal device rather than a dedicated test device.

---

## 8. PRD Tally

19 PRDs now touched (Day 2's 18 + Morning Dashboard), out of 47 in the Phase 3 corpus. Everything from Day 2's tally stands unchanged; adding:

| # | PRD | Doc | Scope actually implemented | Explicitly cut / simplified |
|---|---|---|---|---|
| 19 | Morning Dashboard | 1 | Once-per-day lead item + yesterday recap, low-content fallback | No cross-pillar ranking/Proactivity Ladder/Context Engine - single Finance pillar, no ML |

Plus, not a new PRD but a real cross-cutting deliverable: the Design System (Phase 7) foundations (color roles, type scale, shape) are now actually implemented in code for the first time, rather than existing only as unfilled doc outlines.

---

## 9. Known Gaps and Open Items

Carried forward from Day 2, still true: only ICICI (debit+credit) and SBI (debit) are verified formats; `fallbackToDestructiveMigration()` still wipes non-SMS-derived data (manual entries, budgets, rules, consent, notification read-state) on every schema bump, self-healing only for what can be re-derived from SMS; the OnePlus background-freeze risk for live capture is still unmitigated (though the catch-up scan is now a real safety net against it, not just live capture standing alone).

New after Day 3:
- **Automation Rules, Permissions, Profile, and Night Summary were not re-verified on-device this session** - attempts were repeatedly interrupted by concurrent real device use (Section 7). They compiled successfully as part of the same passing build as everything else in this log, but weren't individually tapped through this round. Search and Notification Center *were* verified (Search returned correct real results for a live query; Notification Center rendered its empty state correctly) - see Day 2's known-gaps for the original list this narrows.
- **SBI's credit pattern (incoming money) is still unverified** - no real sample yet, same caveat as ICICI's credit pattern carried until today.
- **SBI's `senderPatterns` are an unverified guess** - only message body was available to verify against, not the real sender ID.
- **Sohom's actual result is unconfirmed** - the fixes were verified on the dev device with the dev device's own SMS history; whether his ledger is now current is contingent on him installing the fixed build and hasn't been independently confirmed back.
- **Only the "first load" state of the Morning Dashboard was seen; the daily reset (dismissed today, reappears tomorrow) wasn't separately verified** - would require waiting for or faking a real calendar-day boundary.
