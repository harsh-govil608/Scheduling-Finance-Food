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
}
