# Expense Capture Pilot — Android App

Native Kotlin Android app implementing the pilot scoped in
[`docs/mvp-pilot/01-system-design-and-architecture.md`](../docs/mvp-pilot/01-system-design-and-architecture.md).
Read that document first — this README only covers what's implemented and what isn't.

## What's implemented

- **SMS capture**: `sms/SmsReceiver.kt` listens for incoming SMS, hands each message to
  `sms/ParseIncomingSmsWorker.kt` via WorkManager (off the broadcast thread).
- **On-device parsing**: `sms/parser/TransactionParser.kt` + `BankTemplate.kt` — a rule-based
  engine, no ML. One deliberately generic template ships (`BankTemplates.genericTransactionAlert`);
  see "Before you test with real users" below.
- **Categorization**: `categorization/CategorizationEngine.kt` — merchant-lookup-table based,
  learns from corrections via `merchant_rules`.
- **Local-first storage**: Room database (`data/db/`) — transactions, categories, merchant
  rules, and corrections. Nothing requires network connectivity to work.
- **UI**: Jetpack Compose — permission/consent screen, ledger list, recategorize bottom sheet,
  manual-entry dialog (`ui/`).
- **Privacy minimums** (architecture doc Section 9): raw SMS text never persisted or
  transmitted, only structured parsed fields; consent screen explains this in plain language
  before requesting `READ_SMS`/`RECEIVE_SMS`.

## Known gaps — deliberately not yet built

- **Backend sync.** Retrofit is a dependency but no `ApiService`/sync repository exists yet.
  The app is fully local-only right now. This is the natural next increment once the app
  itself is validated on-device.
- **Unparsed-message review queue.** `ParseResult.Unparsed` is currently discarded rather than
  surfaced in the UI for manual confirmation — the architecture doc explicitly calls for this
  (Section 6), it just isn't wired yet.
- **Weekly summary notification.** No push notifications are implemented; `POST_NOTIFICATIONS`
  isn't even declared in the manifest yet, on purpose (permission footprint should match
  implemented functionality, per Section 9).
- **App icon / launcher assets.** No `mipmap` assets exist. Add real launcher icons before
  distributing to pilot testers — Android Studio's built-in Image Asset tool is the fastest way.
- **Gradle wrapper jar.** `gradle/wrapper/gradle-wrapper.properties` exists but the actual
  `gradle-wrapper.jar` binary isn't included (not something a text-based tool can produce
  reliably). Opening this project in Android Studio will regenerate it automatically on sync;
  alternatively run `gradle wrapper --gradle-version 8.7` if you have Gradle installed locally.

## Before you test with real users

1. **Get real sample SMS from your actual pilot cohort's banks/UPI apps.** The one shipped
   template (`BankTemplates.genericTransactionAlert`) is a reasonable starting shape but is
   *not* verified against real bank SMS formats — I did not fabricate specific per-bank regex
   patterns I couldn't verify, since that would be guessing dressed up as engineering. Collect
   3–5 real (redacted) sample messages per bank your cohort uses, and extend `BankTemplate.kt`
   against them before relying on parse-accuracy numbers.
2. **Confirm the Play Store distribution path before assuming public listing.** Google Play's
   permissions policy heavily restricts `READ_SMS`/`RECEIVE_SMS` — apps generally can't publish
   with these permissions on the public Play Store without a permissions-declaration review, and
   many finance apps only get approved for narrow, justified use cases. For a 20-30 person pilot,
   **distribute via a direct APK install or Play Console's closed/internal testing track**, not a
   public listing — that sidesteps the policy review entirely for now and is standard practice
   for private pilots. Revisit this before any wider release.
3. Confirm minSdk 26 (Android 8.0+) covers your pilot cohort's devices.

## Opening the project

Open the `android-app/` folder directly in Android Studio (Iguana or newer recommended given
AGP 8.5 / Kotlin 1.9.24 / compileSdk 34). Let it sync — this regenerates the Gradle wrapper jar
automatically. No `local.properties` is checked in; Android Studio creates it pointing at your
local SDK on first sync.

## Package structure

```
com.lifeos.expensecapture/
├── App.kt, MainActivity.kt
├── data/
│   ├── db/            (Room: entities, DAOs, AppDatabase)
│   ├── repository/     (TransactionRepository)
│   └── seed/            (DefaultCategories)
├── categorization/       (CategorizationEngine)
├── sms/
│   ├── SmsReceiver.kt, ParseIncomingSmsWorker.kt
│   └── parser/          (TransactionParser, BankTemplate)
└── ui/
    ├── theme/
    ├── onboarding/       (PermissionScreen)
    ├── ledger/           (LedgerScreen, LedgerViewModel, CategorizeSheet, ManualEntryDialog)
    └── navigation/       (PilotApp)
```
