plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    // Family module (2026-08): needs a real cross-device backend - Room/SQLite is local-only by
    // design (see AppDatabase's kdoc) and can't sync between different family members' phones.
    // Firebase is the native-Android equivalent of what a Convex+React app would use: Firestore
    // for realtime listeners (Kotlin Flow-wrapped, same shape as every Room DAO Flow already used
    // everywhere else in this app), Auth for cross-device identity, Messaging for push alerts.
    id("com.google.gms.google-services") version "4.4.2" apply false
    // Infra hardening (2026-08, real founder request: "fix these infra issues" after a review of
    // gaps vs. a typical funded startup) - remote crash visibility. Before this, a crash only
    // left a trace in CrashHandler's local Room table on that one device; the founder had no way
    // to know an install crashed at all unless the tester happened to mention it.
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
