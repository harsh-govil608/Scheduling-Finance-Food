package com.lifeos.expensecapture.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ConsentDao
import com.lifeos.expensecapture.data.db.entity.ConsentEntity
import com.lifeos.expensecapture.ui.onboarding.CONSENT_NOTIFICATIONS
import com.lifeos.expensecapture.ui.onboarding.CONSENT_SMS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PermissionRowState(val label: String, val explanation: String, val isGranted: Boolean, val wasRevoked: Boolean)

data class PermissionsUiState(val rows: List<PermissionRowState> = emptyList())

/**
 * Permissions & Consent PRD, Phase 3 Doc 41: "a centralized permissions review screen" plus
 * revocation detection - "as a user who revokes a permission mid-use, I want to immediately
 * understand what stopped working and why." Checks the actual OS-level permission state
 * against the last recorded consent decision every time this screen (or Home) is viewed, since
 * the OS can revoke a permission outside the app's knowledge (Doc 41's own named failure
 * scenario).
 */
class PermissionsViewModel(
    private val context: Context,
    private val consentDao: ConsentDao
) : ViewModel() {

    val uiState: StateFlow<PermissionsUiState> = consentDao.observeAll().map { consents ->
        val smsConsent = consents.firstOrNull { it.permissionType == CONSENT_SMS }
        val notifConsent = consents.firstOrNull { it.permissionType == CONSENT_NOTIFICATIONS }

        val smsGrantedNow = hasSms()
        val notifGrantedNow = hasNotifications()

        PermissionsUiState(
            rows = listOf(
                PermissionRowState(
                    label = "SMS access",
                    explanation = "Reads bank/UPI transaction SMS to auto-capture expenses. Never stores the original message text.",
                    isGranted = smsGrantedNow,
                    wasRevoked = smsConsent?.granted == true && !smsGrantedNow
                ),
                PermissionRowState(
                    label = "Notifications",
                    explanation = "Alerts for bills due, subscription renewals, and over-budget categories you're already tracking.",
                    isGranted = notifGrantedNow,
                    wasRevoked = notifConsent?.granted == true && !notifGrantedNow
                )
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PermissionsUiState())

    fun recordDecision(permissionType: String, granted: Boolean) {
        viewModelScope.launch { consentDao.upsert(ConsentEntity(permissionType, granted)) }
    }

    private fun hasSms(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
