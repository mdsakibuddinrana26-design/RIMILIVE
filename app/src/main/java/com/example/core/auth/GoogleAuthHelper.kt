package com.example.core.auth

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

/**
 * Helper to configure CredentialManager and GoogleIdOption for OAuth 2.0 Google Sign-In.
 */
object GoogleAuthHelper {

    /**
     * Retrieves the configured Web Client ID from string resources, BuildConfig, or defaults.
     */
    fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val value = context.getString(resId)
                if (value.isNotBlank()) value else "759875443958-gamilive.apps.googleusercontent.com"
            } else {
                "759875443958-gamilive.apps.googleusercontent.com"
            }
        } catch (_: Exception) {
            "759875443958-gamilive.apps.googleusercontent.com"
        }
    }

    /**
     * Builds the GoogleIdOption configured for OAuth 2.0 with the provided Web Client ID.
     */
    fun createGoogleIdOption(
        webClientId: String,
        filterByAuthorizedAccounts: Boolean = false,
        autoSelectEnabled: Boolean = false,
        nonce: String? = null
    ): GetGoogleIdOption {
        val builder = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(autoSelectEnabled)

        if (!nonce.isNullOrBlank()) {
            builder.setNonce(nonce)
        }

        return builder.build()
    }

    /**
     * Wraps the GoogleIdOption in a GetCredentialRequest for CredentialManager.
     */
    fun createCredentialRequest(googleIdOption: GetGoogleIdOption): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }
}
