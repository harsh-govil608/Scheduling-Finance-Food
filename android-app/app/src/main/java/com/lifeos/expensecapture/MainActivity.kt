package com.lifeos.expensecapture

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lifeos.expensecapture.ui.navigation.PilotApp
import com.lifeos.expensecapture.ui.theme.ExpenseCaptureTheme

// AppCompatActivity, not a plain ComponentActivity (2026-08 fix - see themes.xml's kdoc on
// Theme.ExpenseCapture for why): AppCompatDelegate.setApplicationLocales needs this base class to
// actually apply/recreate on Android versions below 13. Fully compatible with Compose's setContent
// - AppCompatActivity extends androidx.activity.ComponentActivity too, just with this extra plumbing.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseCaptureTheme {
                // Screens built on Scaffold paint the theme background for free (Scaffold wraps a
                // Surface internally); bare-Column screens like PermissionScreen don't, and were
                // falling through to the window's default light background - found by actually
                // running onboarding after the dark theme refresh, not by reading the screen code.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PilotApp(application as App)
                }
            }
        }
    }
}
