package com.animedong.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Login Google via Credential Manager -> Firebase Auth.
 *
 * TODO: ganti GOOGLE_WEB_CLIENT_ID dengan Web Client ID dari
 * Firebase Console > Authentication > Sign-in method > Google.
 */
const val GOOGLE_WEB_CLIENT_ID = "TODO_WEB_CLIENT_ID.apps.googleusercontent.com"

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    init {
        auth.addAuthStateListener { _user.value = it.currentUser }
    }

    /** Guest mode tetap didukung: jangan panggil ini kalau user pilih lanjut tanpa login. */
    suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = CredentialManager.create(context).getCredential(context, request)
        val googleId = GoogleIdTokenCredential.createFrom(result.credential.data)
        val credential = GoogleAuthProvider.getCredential(googleId.idToken, null)
        auth.signInWithCredential(credential).await()
        Unit
    }

    fun signOut() {
        auth.signOut()
    }
}
