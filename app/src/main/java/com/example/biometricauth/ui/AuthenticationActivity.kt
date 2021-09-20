package com.example.biometricauth.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.biometricauth.R
import com.example.biometricauth.databinding.ActivityMainBinding
import com.example.biometricauth.ui.AuthenticationViewModel.AuthenticatorStatus
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val basicAuthenticationCallback by lazy {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
            ) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey("KEY_NAME", null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setupViewModel()
        setupActions()
    }

    private fun setupViewModel() = binding.run {
        viewModel.state.observe(this@AuthenticationActivity) { state ->
            setupBiometricStatus(biometricStatus.biometricStrong, state.biometricStrongStatus)
            setupBiometricStatus(biometricStatus.biometricWeak, state.biometricWeakStatus)
            setupBiometricStatus(biometricStatus.deviceCredential, state.deviceCredentialStatus)

            authBiometricStrong.isEnabled = state.biometricStrongStatus is AuthenticatorStatus.Success
            authBiometricWeak.isEnabled = state.biometricWeakStatus is AuthenticatorStatus.Success
            authDeviceCredential.isEnabled = state.deviceCredentialStatus is AuthenticatorStatus.Success
        }
    }

    private fun setupBiometricStatus(view: MaterialTextView, status: AuthenticatorStatus) {
        view.text = (status as? AuthenticatorStatus.Failure)?.message
        view.setCompoundDrawablesWithIntrinsicBounds(status.icon, 0, 0, 0)
        view.setTextColor(getColor(status.color))
    }

    private fun setupActions() = binding.run {
        authBiometricStrong.setOnClickListener { runBasicAuthentication(strongPromptInfo) }
        authBiometricWeak.setOnClickListener { runBasicAuthentication(weakPromptInfo)  }
        authDeviceCredential.setOnClickListener { runBasicAuthentication(credentialPromptInfo) }
    }

    private fun runBasicAuthentication(promptInfo: BiometricPrompt.PromptInfo) =
            BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    basicAuthenticationCallback
            ).authenticate(promptInfo)

    private fun runCryptoAuthentication(promptInfo: BiometricPrompt.PromptInfo) {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                basicAuthenticationCallback
        ).authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(cipher)
        )
    }

    private val basePromptInfo
        get() = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_login))
                .setSubtitle(getString(R.string.biometric_login_to_show_secret_screen))

    private val strongPromptInfo
        get() = basePromptInfo
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.use_password))
                .setConfirmationRequired(true)
                .build()

    private val weakPromptInfo
        get() = basePromptInfo
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .setNegativeButtonText(getString(R.string.use_password))
                .setConfirmationRequired(true)
                .build()

    private val credentialPromptInfo
        get() = basePromptInfo
                .setAllowedAuthenticators(DEVICE_CREDENTIAL)
                .build()
}