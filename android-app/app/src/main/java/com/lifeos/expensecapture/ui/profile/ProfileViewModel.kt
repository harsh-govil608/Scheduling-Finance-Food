package com.lifeos.expensecapture.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(val displayName: String = "", val capturePaused: Boolean = false)

/**
 * Account & Profile Management PRD, Phase 3 Doc 44, scoped to what's achievable with no
 * backend/auth: no account credentials exist to manage. What DOES apply: a display name, the
 * centralized pause control the User Control Model calls for, and "delete my account" -
 * reinterpreted honestly for a local-only app as "delete all locally stored data," since
 * there's no server-side account to delete in the first place.
 */
class ProfileViewModel(private val context: Context, private val database: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            displayName = Prefs.getDisplayName(context),
            capturePaused = Prefs.isCapturePaused(context)
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun setDisplayName(name: String) {
        Prefs.setDisplayName(context, name)
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun setCapturePaused(paused: Boolean) {
        Prefs.setCapturePaused(context, paused)
        _uiState.value = _uiState.value.copy(capturePaused = paused)
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            database.clearAllTables()
            database.categoryDao().insertAll(DefaultCategories.asEntities())
            Prefs.setCapturePaused(context, false)
            onDone()
        }
    }
}
