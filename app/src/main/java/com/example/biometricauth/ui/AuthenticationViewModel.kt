package com.example.biometricauth.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.biometricauth.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
        private val biometricManager: BiometricManager,
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    data class State(
            val biometricStrongStatus: AuthenticatorStatus,
            val biometricWeakStatus: AuthenticatorStatus,
            val deviceCredentialStatus: AuthenticatorStatus,
    )

    sealed class AuthenticatorStatus {
        object Success : AuthenticatorStatus()
        class Failure(val message: String) : AuthenticatorStatus()

        val icon: Int
            get() = if (this is Success) R.drawable.ic_check else R.drawable.ic_close

        val color: Int
            get() = if (this is Success) R.color.green else R.color.red
    }

    init {
        _state.value = State(
                getAuthenticatorStatus(BIOMETRIC_STRONG),
                getAuthenticatorStatus(BIOMETRIC_WEAK),
                getAuthenticatorStatus(DEVICE_CREDENTIAL),
        )
    }

    private fun getAuthenticatorStatus(authenticator: Int) =
            convertBiometricStatus(biometricManager.canAuthenticate(authenticator))

    private fun convertBiometricStatus(result: Int) = when (result) {
        BiometricManager.BIOMETRIC_SUCCESS -> AuthenticatorStatus.Success
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> AuthenticatorStatus.Failure(message = "BIOMETRIC_STATUS_UNKNOWN")
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> AuthenticatorStatus.Failure(message = "BIOMETRIC_ERROR_UNSUPPORTED")
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthenticatorStatus.Failure(message = "BIOMETRIC_ERROR_HW_UNAVAILABLE")
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthenticatorStatus.Failure(message = "BIOMETRIC_ERROR_NONE_ENROLLED")
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthenticatorStatus.Failure(message = "BIOMETRIC_ERROR_NO_HARDWARE")
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> AuthenticatorStatus.Failure(message = "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
        else -> AuthenticatorStatus.Failure(message = "UNKNOWN_ERROR")

    }


}