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
import com.google.firebase.Firebase
//import com.example.voicetranslator.R
//import com.example.voicetranslator.data.UserData
//import com.google.android.gms.auth.api.identity.BeginSignInRequest
//import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

// Represents the authentication state for the UI
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userData: UserData) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * This ViewModel now handles BOTH state management and direct interaction
 * with the Google Sign-In services.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth

    // --- Client-side Logic (Previously GoogleAuthUiClient) ---
    private val oneTapClient = Identity.getSignInClient(application)

    // --- State Management Logic ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    /**
     * Starts the Google Sign-In process and returns an IntentSender for the UI to launch.
     */
    suspend fun getSignInIntent(): IntentSender? {
        return try {
            oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()
            ).await().pendingIntent.intentSender
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            _authState.value = AuthState.Error(e.message ?: "Could not start sign-in flow")
            null
        }
    }

    /**
     * Processes the result from the Sign-In intent and authenticates with Firebase.
     */
    fun processSignInResult(intent: Intent) {
        viewModelScope.launch {
            try {
                val credentialResponse = oneTapClient.getSignInCredentialFromIntent(intent)
                val googleIdToken = credentialResponse.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(firebaseCredential).await()
                    checkCurrentUser() // Update state to Authenticated
                } else {
                    _authState.value = AuthState.Error("Google Sign-In failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred during sign-in")
            }
        }
    }

    /**
     * Signs the user out from both Firebase and Google One-Tap.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                oneTapClient.signOut().await()
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                // Even if Google sign-out fails, update our app's state
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Checks if a user is already logged in when the ViewModel is created.
     */
    private fun checkCurrentUser() {
        auth.currentUser?.let { firebaseUser ->
            _authState.value = AuthState.Authenticated(
                userData = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )
            )
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
