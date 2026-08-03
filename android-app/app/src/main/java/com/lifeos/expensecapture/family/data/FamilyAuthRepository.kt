package com.lifeos.expensecapture.family.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class AuthResult(val success: Boolean, val errorMessage: String? = null)

/** Mirrors PhoneAuthProvider's own callback shape - see [FamilyAuthRepository.sendOtp]'s kdoc for
 * why this stays callback-based rather than a single suspend call. */
sealed class OtpSendResult {
    data class CodeSent(val verificationId: String) : OtpSendResult()
    /** Some devices/carriers auto-detect the SMS and verify without the user ever typing a code -
     * the UI should skip straight to signed-in when this fires, never showing an OTP field at all. */
    data class AutoVerified(val credential: PhoneAuthCredential) : OtpSendResult()
    data class Failed(val message: String) : OtpSendResult()
}

/**
 * Family module (2026-08) - the one piece of cross-device identity this app never needed before:
 * every other screen operates on a single local device with no accounts (see AppDatabase's kdoc,
 * ProfileViewModel's kdoc on "no account credentials exist to manage"). Family sharing is
 * impossible without SOME identity a second phone can also authenticate as, so this repository
 * exists scoped entirely to the family module - it does not touch or replace the local
 * Prefs.getDisplayName()-based "profile" used everywhere else.
 *
 * Phone number + OTP rather than email/password (real user request, 2026-08 - matches how
 * PhonePe/GPay/WhatsApp all work in India, and matches "invite by phone number" already being
 * the identity anchor for both this module and Smart Split's user lookup). Needs the phone
 * project's SHA-1 fingerprint registered in the Firebase console for silent Play Integrity
 * verification - falls back to a reCAPTCHA web view automatically if that's not set up, so this
 * still works during initial testing without it.
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

    /**
     * Callback-based, not suspend: [PhoneAuthProvider.verifyPhoneNumber] can invoke its callback
     * more than once for a single call (e.g. [OtpSendResult.CodeSent] followed later by a resend
     * timeout), which a single-shot suspend function can't represent - the caller (
     * FamilySignInScreen) needs to react differently to each case anyway (auto-verified skips
     * the OTP screen entirely; code-sent shows it).
     */
    fun sendOtp(phoneNumber: String, activity: Activity, onResult: (OtpSendResult) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    onResult(OtpSendResult.AutoVerified(credential))
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    onResult(OtpSendResult.Failed(exception.message ?: "Couldn't send the code"))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    onResult(OtpSendResult.CodeSent(verificationId))
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(verificationId: String, code: String): AuthResult {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithCredential(credential)
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.message ?: "That code doesn't look right")
        }
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential): AuthResult {
        return try {
            auth.signInWithCredential(credential).await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.message ?: "Sign in failed")
        }
    }

    /** Phone auth never populates displayName the way email signup with a name field could -
     * FamilySignInScreen asks for it once, right after first-ever verification. */
    suspend fun updateDisplayName(name: String): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult(false, "Not signed in")
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.message ?: "Couldn't save your name")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
