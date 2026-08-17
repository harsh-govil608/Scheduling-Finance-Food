import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Keeps the OpenRouter key (and anything else secret) out of git entirely - local.properties is
// already gitignored for sdk.dir, so it's read here rather than committing a key to build.gradle.kts
// or anywhere under source control. Falls back to an empty string when missing, which AiClient
// treats as "no AI configured" everywhere it's used.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}

// Release signing (2026-08 infra hardening) - same local/gitignored-file pattern as
// localProperties above. Deliberately optional: a fresh checkout (or CI, which never has this
// file - see .github/workflows/android-ci.yml, which only builds/tests the debug variant) must
// still be able to run `assembleDebug`/tests without it. Only `assembleRelease` actually needs
// these values, and that's gated below on the file existing at all.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(FileInputStream(file))
}
val hasReleaseSigning = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "com.lifeos.expensecapture"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lifeos.expensecapture"
        minSdk = 26
        targetSdk = 34
        versionCode = 98
        versionName = "0.60.8-pilot"
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${localProperties.getProperty("OPENROUTER_API_KEY", "")}\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // google-auth-library-oauth2-http and google-auth-library-credentials (transitive
            // deps of the Drive API client, 2026-08-15) both bundle an identical META-INF/
            // INDEX.LIST - packaging conflict, not a real difference between the two jars.
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    // Per-app language preference (2026-08, real user request: Hindi/English switchable at
    // first open) - AppCompatDelegate.setApplicationLocales works even on a plain
    // ComponentActivity (this app has never used AppCompatActivity/Views) as of appcompat 1.6+,
    // which auto-registers a manifest service that handles locale persistence/recreation across
    // both pre- and post-Android-13 without this app needing its own Context-wrapping logic.
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Not wired yet in this scaffold pass - present for the backend-sync increment.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Family module (2026-08): the "backend-sync increment" the retrofit deps above were staged
    // for - Firestore for realtime cross-device data (Kotlin Flow-wrapped listeners), Auth for
    // family-member identity, Messaging for SOS/reminder push alerts. See build.gradle.kts'
    // top-level plugin comment for why Firebase over Room for this module specifically.
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    // Family member photos (2026-08, real user report) - the app's own ProfileAvatarButton only
    // ever loads a local file path via BitmapFactory; Coil is the lightweight standard for
    // loading a remote Firebase Storage download URL (member.photoUrl) inside Compose.
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Infra hardening (2026-08) - see AppLogger.e's kdoc for how this complements, not replaces,
    // the local Room-backed crash_logs table CrashHandler already writes to.
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Auto Backup to Google Drive (2026-08-15, real user request, standing backlog item) - Drive
    // REST API access via a signed-in Google account, scoped to Drive.file (files this app itself
    // creates only, not the user's whole Drive) so it qualifies for Google's lighter OAuth
    // verification path rather than a full sensitive-scope review. See DriveBackupRepository's
    // kdoc for the one-time Google Cloud Console setup this still needs before it works.
    //
    // Real P0 bug (2026-08-15, found via a real device - app crashed on launch for every user,
    // not just Drive backup): google-api-client-android transitively pulls in
    // google-auth-library-oauth2-http, which pulls in a grpc-okhttp version that conflicts with
    // the grpc version Cloud Firestore itself needs - Gradle silently resolved to a jar missing
    // io.grpc.InternalGlobalInterceptors, which Firestore's channel init requires and which this
    // app touches very early in its startup path (FamilyAppViewModel's auth-state listener), so
    // every launch crashed with a NoClassDefFoundError before any UI ever appeared. This Drive
    // integration only ever uses plain REST/HTTP (NetHttpTransport + GoogleAccountCredential),
    // never grpc directly - excluding it here is safe and lets Firestore's own required version
    // win instead of being silently overridden.
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:2.7.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "io.grpc")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev20240914-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "io.grpc")
    }
    implementation("com.google.http-client:google-http-client-gson:1.45.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "io.grpc")
    }

    // Monetization scaffolding (2026-08-12, real founder request - "implementation to be paid").
    // Only functions once this app is installed via Google Play (even just the internal testing
    // track) - a sideloaded APK, which is everything distributed so far via distribution/, will
    // never be able to complete a real purchase through this library. See BillingRepository's kdoc.
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
