package com.lifeos.expensecapture.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ProfileUiState(
    val displayName: String = "",
    val capturePaused: Boolean = false,
    val profilePhotoPath: String? = null
)

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
            capturePaused = Prefs.isCapturePaused(context),
            profilePhotoPath = Prefs.getProfilePhotoPath(context)
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun setDisplayName(name: String) {
        Prefs.setDisplayName(context, name)
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    /** Copies the picker's selected image into app-internal storage rather than keeping its
     * content:// Uri directly - the system photo picker's read grant isn't guaranteed to survive
     * process death/reboot, so the Uri alone isn't a durable reference. */
    fun setProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                val target = File(context.filesDir, "profile_photo.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                target.absolutePath
            }
            Prefs.setProfilePhotoPath(context, path)
            _uiState.value = _uiState.value.copy(profilePhotoPath = path)
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uiState.value.profilePhotoPath?.let { File(it).delete() }
            }
            Prefs.setProfilePhotoPath(context, null)
            _uiState.value = _uiState.value.copy(profilePhotoPath = null)
        }
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
