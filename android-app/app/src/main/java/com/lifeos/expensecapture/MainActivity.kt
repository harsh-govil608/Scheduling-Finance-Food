package com.lifeos.expensecapture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lifeos.expensecapture.ui.navigation.PilotApp
import com.lifeos.expensecapture.ui.theme.ExpenseCaptureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseCaptureTheme {
                PilotApp(application as App)
            }
        }
    }
}
