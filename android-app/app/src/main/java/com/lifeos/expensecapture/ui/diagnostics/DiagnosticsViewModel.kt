package com.lifeos.expensecapture.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CrashLogDao
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pre-beta hardening (Priority 2): the read side of the local crash log - without this, every
 * record CrashHandler/AppLogger write would be inert, visible only by pulling the raw database
 * file the way this whole project's manual verification has depended on all along. This is what
 * makes the log actually useful without needing physical access to the device.
 */
class DiagnosticsViewModel(private val crashLogDao: CrashLogDao) : ViewModel() {

    val entries: StateFlow<List<CrashLogEntity>> = crashLogDao.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch { crashLogDao.clearAll() }
    }
}
