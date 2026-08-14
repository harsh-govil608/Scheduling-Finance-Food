package com.lifeos.expensecapture.family.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.FamilyResult
import com.lifeos.expensecapture.family.model.FamilyEntity
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.ui.normalizePhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FamilyAppUiState(
    val isSignedIn: Boolean = false,
    val userId: String? = null,
    val userDisplayName: String = "",
    val userPhoneNumber: String = "",
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
    private val familyRepository: FamilyRepository = FamilyRepository(),
    private val splitPayRepository: SplitPayRepository = SplitPayRepository()
) : ViewModel() {

    init {
        // Keeps Smart Split's own users/{uid} doc (phoneNumber/displayName) in step with the
        // Firebase Auth account on every sign-in - see SplitPayRepository.syncPhoneAndName's
        // kdoc for why this can't just be read directly off FirebaseAuth at lookup time instead
        // (Smart Split's findUserByPhone needs its own indexed Firestore field to query against).
        viewModelScope.launch {
            authRepository.observeAuthState().filterNotNull().collect { user ->
                splitPayRepository.syncPhoneAndName(
                    uid = user.uid,
                    phoneNumber = user.phoneNumber?.let { normalizePhoneNumber(it) },
                    displayName = user.displayName ?: ""
                )
                // Self-heals every FamilyMember doc this user belongs to against the Firebase
                // Auth profile on every sign-in (real user report, 2026-08: member name/photo
                // should reflect whatever's actually set, not a stale one-time snapshot from
                // whenever they first created/joined - see syncMemberProfile's kdoc). Covers the
                // case where the name/photo changed on a different device since this one last
                // synced, same reasoning as syncPhoneAndName above.
                familyRepository.syncMemberProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
            }
        }
    }

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
                            userDisplayName = u.displayName ?: "",
                            userPhoneNumber = u.phoneNumber ?: "",
                            families = families,
                            loading = false
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyAppUiState())

    /** Needs a display name for anyone who just verified for the first time - phone auth never
     * populates one the way an email-signup form could ask for it directly. FamilySignInScreen
     * shows a one-time name prompt right after verification when this is true. */
    fun needsDisplayName(): Boolean = authRepository.currentUser?.displayName.isNullOrBlank()

    fun setDisplayName(name: String) {
        viewModelScope.launch { authRepository.updateDisplayName(name) }
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
