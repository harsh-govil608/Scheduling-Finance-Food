package com.lifeos.expensecapture.family.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.FamilyResult
import com.lifeos.expensecapture.family.model.FamilyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FamilyAppUiState(
    val isSignedIn: Boolean = false,
    val userId: String? = null,
    val userDisplayName: String = "",
    val userEmail: String = "",
    val families: List<FamilyEntity> = emptyList(),
    val loading: Boolean = true
)

/**
 * Family module's entry point (2026-08) - decides which of the three top-level states to show:
 * signed out -> FamilySignInScreen, signed in with no families -> FamilyOnboardingScreen, signed
 * in with a family -> FamilyDashboardScreen. Multi-family switching (a user in more than one
 * family) uses the first family in [FamilyAppUiState.families] for this pass - a real switcher
 * is a reasonable fast-follow once the single-family flow is proven, not a gap in the data model
 * (FamilyRepository.observeUserFamilies already returns every family a user belongs to).
 */
class FamilyAppViewModel(
    private val authRepository: FamilyAuthRepository = FamilyAuthRepository(),
    private val familyRepository: FamilyRepository = FamilyRepository()
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FamilyAppUiState> = authRepository.observeAuthState()
        .flatMapLatest { user ->
            if (user == null) {
                kotlinx.coroutines.flow.flowOf(FamilyAppUiState(isSignedIn = false, loading = false))
            } else {
                familyRepository.observeUserFamilies(user.uid).let { familiesFlow ->
                    kotlinx.coroutines.flow.combine(
                        kotlinx.coroutines.flow.flowOf(user),
                        familiesFlow
                    ) { u, families ->
                        FamilyAppUiState(
                            isSignedIn = true,
                            userId = u.uid,
                            userDisplayName = u.displayName ?: u.email?.substringBefore("@") ?: "",
                            userEmail = u.email ?: "",
                            families = families,
                            loading = false
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyAppUiState())

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            _authError.value = if (!result.success) result.errorMessage else null
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, displayName)
            _authError.value = if (!result.success) result.errorMessage else null
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    private val _familyActionError = MutableStateFlow<String?>(null)
    val familyActionError: StateFlow<String?> = _familyActionError

    fun createFamily(name: String, onCreated: (String) -> Unit) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            when (val result = familyRepository.createFamily(name, user.uid, uiState.value.userDisplayName)) {
                is FamilyResult.Success -> onCreated(result.value)
                is FamilyResult.Failure -> _familyActionError.value = result.message
            }
        }
    }

    fun joinFamily(code: String, onJoined: (String) -> Unit) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            when (val result = familyRepository.joinFamilyByCode(code, user.uid, uiState.value.userDisplayName)) {
                is FamilyResult.Success -> onJoined(result.value)
                is FamilyResult.Failure -> _familyActionError.value = result.message
            }
        }
    }
}
