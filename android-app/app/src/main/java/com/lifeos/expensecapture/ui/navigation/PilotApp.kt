package com.lifeos.expensecapture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.ledger.LedgerScreen
import com.lifeos.expensecapture.ui.onboarding.PermissionScreen

@Composable
fun PilotApp(app: App) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "permission") {
        composable("permission") {
            PermissionScreen(
                onGranted = {
                    navController.navigate("ledger") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
        composable("ledger") {
            LedgerScreen(app = app)
        }
    }
}
