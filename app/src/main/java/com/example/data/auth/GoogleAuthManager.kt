package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

data class GoogleUserData(
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String?
)

sealed class AuthResult {
    data class Success(val user: GoogleUserData) : AuthResult()
    data class Cancelled(val message: String = "Login dibatalkan") : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(webClientId: String = ""): AuthResult {
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            // Standard Google Client ID or default web client ID
            val effectiveClientId = if (webClientId.isNotBlank()) {
                webClientId
            } else {
                // If not provided in environment, use standard placeholder or fallback
                "953036384403-placeholder.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                AuthResult.Success(
                    GoogleUserData(
                        email = googleIdToken.id,
                        displayName = googleIdToken.displayName ?: googleIdToken.id.substringBefore("@"),
                        photoUrl = googleIdToken.profilePictureUri?.toString(),
                        idToken = googleIdToken.idToken
                    )
                )
            } else {
                AuthResult.Error("Tipe kredensial tidak dikenali")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("GoogleAuthManager", "User cancelled Google Sign-In", e)
            AuthResult.Cancelled("Pemilihan akun Google dibatalkan")
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "GetCredentialException: ${e.message}", e)
            AuthResult.Error(e.message ?: "Gagal mendapatkan akun Google dari perangkat")
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Unexpected Google Sign-In error", e)
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat menghubungkan akun Google")
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("GoogleAuthManager", "Error clearing credential state", e)
        }
    }
}
