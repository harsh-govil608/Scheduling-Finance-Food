package com.lifeos.expensecapture.family.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.expensecapture.family.ui.dashboard.FamilyDashboardScreen

/**
 * Family module's single entry point (2026-08) - registered once in PilotApp.kt's NavHost as
 * "family", reached from Profile ("Family" row, not a 6th bottom-nav tab - see PilotApp.kt's
 * routing comment for why). Decides which of three states to show; see FamilyAppViewModel's kdoc.
 * Multi-family switching uses the first family in the list for this pass.
 */
@Composable
fun FamilyEntryScreen(
    onOpenTasks: (String) -> Unit,
    onOpenCalendar: (String) -> Unit,
    onOpenExpenses: (String) -> Unit,
    onOpenDocuments: (String) -> Unit,
    onOpenHealth: (String) -> Unit,
    onOpenEmergencyContacts: (String) -> Unit,
    onOpenMembers: (String) -> Unit,
    onOpenInvite: (String) -> Unit,
    onOpenSos: (String) -> Unit,
    onOpenNotifications: (String) -> Unit
) {
    val viewModel = remember { FamilyAppViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        !uiState.isSignedIn -> FamilySignInScreen(viewModel)
        uiState.families.isEmpty() -> FamilyOnboardingScreen(viewModel, onFamilyReady = { })
        else -> {
            val family = uiState.families.first()
            FamilyDashboardScreen(
                familyId = family.id,
                currentUserId = uiState.userId ?: "",
                onOpenTasks = { onOpenTasks(family.id) },
                onOpenCalendar = { onOpenCalendar(family.id) },
                onOpenExpenses = { onOpenExpenses(family.id) },
                onOpenDocuments = { onOpenDocuments(family.id) },
                onOpenHealth = { onOpenHealth(family.id) },
                onOpenEmergencyContacts = { onOpenEmergencyContacts(family.id) },
                onOpenMembers = { onOpenMembers(family.id) },
                onOpenInvite = { onOpenInvite(family.id) },
                onOpenSos = { onOpenSos(family.id) },
                onOpenNotifications = { onOpenNotifications(family.id) }
            )
        }
    }
}
