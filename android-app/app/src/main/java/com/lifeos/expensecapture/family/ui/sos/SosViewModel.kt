package com.lifeos.expensecapture.family.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.FamilyResult
import com.lifeos.expensecapture.family.data.SosRepository
import com.lifeos.expensecapture.family.model.GeoPoint
import com.lifeos.expensecapture.family.model.SOSAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SosUiState(
    val triggering: Boolean = false,
    val lastError: String? = null,
    val justTriggered: Boolean = false
)

/**
 * SOS workflow (2026-08 Family module PRD: "live location sharing and emergency notifications").
 * Location capture itself is the caller's job (SosScreen owns the FusedLocationProviderClient
 * call, since that needs an Android Context/Activity this plain ViewModel shouldn't depend on) -
 * this class is the write path once a GeoPoint (or null, if location permission was denied - an
 * SOS without location is still better than blocking the alert entirely) is in hand.
 */
class SosViewModel(
    private val familyId: String,
    private val userId: String,
    private val userName: String,
    private val sosRepository: SosRepository = SosRepository(),
    private val familyRepository: FamilyRepository = FamilyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SosUiState())
    val uiState: StateFlow<SosUiState> = _uiState

    val activeAlerts: StateFlow<List<SOSAlert>> = sosRepository.observeActiveAlerts(familyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerSos(location: GeoPoint?) {
        // Real bug fixed 2026-08-15: justTriggered was never reset to false anywhere - a second
        // trigger attempt that failed left the stale "Alert sent" success state showing alongside
        // the new error, permanently, for the rest of the screen's lifetime.
        _uiState.value = _uiState.value.copy(triggering = true, lastError = null, justTriggered = false)
        viewModelScope.launch {
            // One-shot read of current membership rather than staying subscribed - SOS only
            // needs "who to notify right now," not an ongoing membership Flow.
            val memberIds = try {
                familyRepository.observeMembers(familyId).first().map { it.userId }
            } catch (e: Exception) {
                emptyList()
            }
            when (val result = sosRepository.triggerSos(familyId, userId, userName, location, memberIds)) {
                is FamilyResult.Success -> _uiState.value = _uiState.value.copy(triggering = false, justTriggered = true)
                is FamilyResult.Failure -> _uiState.value = _uiState.value.copy(triggering = false, justTriggered = false, lastError = result.message)
            }
        }
    }

    fun resolve(alertId: String) {
        viewModelScope.launch {
            sosRepository.resolveSos(familyId, alertId, userId, userName)
        }
    }
}
