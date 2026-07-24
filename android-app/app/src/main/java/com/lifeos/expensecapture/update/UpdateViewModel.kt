package com.lifeos.expensecapture.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateUiState(
    val available: UpdateInfo? = null,
    val downloading: Boolean = false
)

class UpdateViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = UpdateUiState(available = UpdateChecker.checkForUpdate(context))
        }
    }

    fun installUpdate() {
        val update = _uiState.value.available ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(downloading = true)
            try {
                UpdateChecker.downloadAndLaunchInstall(context, update)
            } finally {
                _uiState.value = _uiState.value.copy(downloading = false)
            }
        }
    }

    fun dismiss() {
        _uiState.value = UpdateUiState()
    }
}
