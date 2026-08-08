package com.lifeos.expensecapture.ui.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CrashLogDao
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.sms.SmsDiagnosticsScanner
import com.lifeos.expensecapture.sms.SmsScanDiagnostics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pre-beta hardening (Priority 2): the read side of the local crash log - without this, every
 * record CrashHandler/AppLogger write would be inert, visible only by pulling the raw database
 * file the way this whole project's manual verification has depended on all along. This is what
 * makes the log actually useful without needing physical access to the device.
 */
class DiagnosticsViewModel(
    private val context: Context,
    private val crashLogDao: CrashLogDao
) : ViewModel() {

    val entries: StateFlow<List<CrashLogEntity>> = crashLogDao.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch { crashLogDao.clearAll() }
    }

    private val _smsScanState = MutableStateFlow<SmsScanState>(SmsScanState.Idle)
    val smsScanState: StateFlow<SmsScanState> = _smsScanState.asStateFlow()

    /** SMS capture audit (2026-08, real founder request): "add or improve a simple SMS
     * diagnostics/debug function ... so we can see" what SmsHistoryScanner/TransactionIngestor
     * actually did with the inbox, without needing to pull the raw database file. */
    fun runSmsScanDiagnostics() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            _smsScanState.value = SmsScanState.PermissionMissing
            return
        }
        _smsScanState.value = SmsScanState.Running
        viewModelScope.launch {
            _smsScanState.value = try {
                SmsScanState.Result(SmsDiagnosticsScanner.scan(context))
            } catch (e: Exception) {
                AppLogger.e("DiagnosticsViewModel", "SMS scan diagnostics failed", e)
                SmsScanState.Failed
            }
        }
    }
}

sealed class SmsScanState {
    object Idle : SmsScanState()
    object Running : SmsScanState()
    object Failed : SmsScanState()
    object PermissionMissing : SmsScanState()
    data class Result(val diagnostics: SmsScanDiagnostics) : SmsScanState()
}
