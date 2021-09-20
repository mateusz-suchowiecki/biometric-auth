package com.example.biometricauth.common

import androidx.biometric.BiometricPrompt


abstract class SimpleBiometricAuthenticationCallback : BiometricPrompt.AuthenticationCallback() {
    abstract fun onError(message: String)

    override fun onAuthenticationFailed() =
            onError("Authentication failed")

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
            onError("Authentication error: $errString")
}