package com.lifeos.expensecapture.family.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthResult(val success: Boolean, val errorMessage: String? = null)

/**
 * Family module (2026-08) - the one piece of cross-device identity this app never needed before:
 * every other screen operates on a single local device with no accounts (see AppDatabase's kdoc,
 * ProfileViewModel's kdoc on "no account credentials exist to manage"). Family sharing is
 * impossible without SOME identity a second phone can also authenticate as, so this repository
 * exists scoped entirely to the family module - it does not touch or replace the local
 * Prefs.getDisplayName()-based "profile" used everywhere else.
 *
 * Email/password rather than Google Sign-In: keeps the dependency surface to just
 * firebase-auth-ktx (no Google Identity Services setup, no SHA-1 fingerprint registration step
 * beyond what Firebase itself needs), and matches "invite via email" already using email as the
 * family's own identity anchor.
 */
class FamilyAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Emits the current user (or null) immediately, then again on every sign-in/out - the
     * Convex-subscription-equivalent Flow every other repository in this module also exposes. */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            result.user?.updateProfile(profileUpdate)?.await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.message ?: "Sign in failed")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
