// AuthViewModel.kt
package com.example.ai_voice_translater.data

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_voice_translater.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userData: UserData) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val primaryAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val oneTapClient = Identity.getSignInClient(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    init {
        primaryAuth.addAuthStateListener { checkCurrentUser() }
    }

    // ... (getSignInIntent, processSignInResult, signOut methods remain the same)
    suspend fun getSignInIntent(): IntentSender? {
        return try {
            val request = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()
            oneTapClient.beginSignIn(request).await().pendingIntent.intentSender
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _authState.value = AuthState.Error(e.message ?: "Could not start sign-in flow")
            null
        }
    }

    fun processSignInResult(intent: Intent) {
        viewModelScope.launch {
            try {
                val credentialResponse = oneTapClient.getSignInCredentialFromIntent(intent)
                val googleIdToken = credentialResponse.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    primaryAuth.signInWithCredential(firebaseCredential).await()
                } else {
                    _authState.value = AuthState.Error("Google Sign-In failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred during sign-in")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                oneTapClient.signOut().await()
                primaryAuth.signOut()
                _authState.value = AuthState.Unauthenticated;
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                primaryAuth.signOut()
            }
        }
    }


    // Updated function to handle both name and photo updates
    suspend fun updateUserProfile(newName: String) {
        val user = primaryAuth.currentUser ?: return
        try {

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(profileUpdates).await()
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to update profile")
        }
    }

    private fun checkCurrentUser() {
        primaryAuth.currentUser?.let { firebaseUser ->
            _authState.value = AuthState.Authenticated(
                userData = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    profilePictureUrl = firebaseUser.photoUrl?.toString(),
                    email = firebaseUser.email.toString()
                )
            )
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private suspend fun <T> Task<T>.await(): T {
        return suspendCancellableCoroutine { cont ->
            addOnCompleteListener {
                if (cont.isCancelled) return@addOnCompleteListener
                if (it.isSuccessful) {
                    cont.resume(it.result)
                } else {
                    cont.resumeWithException(it.exception ?: Exception("Unknown Task error"))
                }
            }
        }
    }
}
