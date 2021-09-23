package com.example.biometricauth.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.biometricauth.R
import com.example.biometricauth.common.Event
import com.example.biometricauth.common.SimpleBiometricAuthenticationCallback
import com.example.biometricauth.di.Crypto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
        private val biometricManager: BiometricManager,
        private val crypto: Crypto,
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _cryptoData = MutableLiveData<String>()
    val cryptoData: LiveData<String> = _cryptoData

    private val _statusData = MutableLiveData<Event<String>>()
    val statusData: LiveData<Event<String>> = _statusData

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
        crypto.generateSecretKey()
        _state.value = State(
                getAuthenticatorStatus(BIOMETRIC_STRONG),
                getAuthenticatorStatus(BIOMETRIC_WEAK),
                getAuthenticatorStatus(DEVICE_CREDENTIAL),
        )
    }

    val encryptCryptoObject: BiometricPrompt.CryptoObject
        get() = BiometricPrompt.CryptoObject(crypto.cipherInEncryptMode())

    val decryptCryptoObject: BiometricPrompt.CryptoObject
        get() = BiometricPrompt.CryptoObject(crypto.cipherInDecryptMode())

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

    val basicAuthenticationCallback by lazy {
        object : SimpleBiometricAuthenticationCallback() {
            override fun onError(message: String) = _statusData.postValue(Event(message))
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                _statusData.postValue(Event("Success"))
            }
        }
    }

    fun encryptAuthenticationCallback(data: String) = object : SimpleBiometricAuthenticationCallback() {
        override fun onError(message: String) = _statusData.postValue(Event(message))
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val encryptedData = result.cryptoObject?.cipher?.doFinal(
                    data.toByteArray(Charset.defaultCharset())
            )

            encryptedData?.let {
                _cryptoData.value = Base64.getEncoder().encodeToString(encryptedData)
                _statusData.postValue(Event("Success"))
            }
        }
    }

    fun decryptAuthenticationCallback(encryptedData: String?) = object : SimpleBiometricAuthenticationCallback() {
        override fun onError(message: String) = _statusData.postValue(Event(message))
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            encryptedData ?: return

            val data = result.cryptoObject?.cipher?.doFinal(
                    Base64.getDecoder().decode(encryptedData)
            )

            data?.let {
                _cryptoData.value = data.decodeToString()
                _statusData.postValue(Event("Success"))
            }
        }
    }
}