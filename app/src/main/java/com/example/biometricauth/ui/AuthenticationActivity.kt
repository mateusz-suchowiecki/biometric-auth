package com.example.biometricauth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.biometricauth.R
import com.example.biometricauth.databinding.ActivityMainBinding
import com.example.biometricauth.ui.AuthenticationViewModel.AuthenticatorStatus
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

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

        viewModel.statusData.observe(this@AuthenticationActivity) { event ->
            Toast.makeText(applicationContext, event.getContentIfNotHandled(), Toast.LENGTH_SHORT).show()
        }

        viewModel.cryptoData.observe(this@AuthenticationActivity) { data ->
            cryptoData.text = data
            decryptButton.isEnabled = data != null
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

        encryptButton.setOnClickListener { runEncryptAuthentication(encryptPromptInfo) }
        decryptButton.setOnClickListener { runDecryptAuthentication(decryptPromptInfo) }
    }

    private fun runBasicAuthentication(promptInfo: BiometricPrompt.PromptInfo) =
            BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    viewModel.basicAuthenticationCallback
            ).authenticate(promptInfo)

    private fun runEncryptAuthentication(promptInfo: BiometricPrompt.PromptInfo) {
        BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                viewModel.encryptAuthenticationCallback("Secret data")
        ).authenticate(
                promptInfo,
                viewModel.encryptCryptoObject
        )
    }

    private fun runDecryptAuthentication(promptInfo: BiometricPrompt.PromptInfo) {
        BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                viewModel.decryptAuthenticationCallback(viewModel.cryptoData.value)
        ).authenticate(
                promptInfo,
                viewModel.decryptCryptoObject
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

    private val encryptPromptInfo
        get() = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_encrypt))
                .setSubtitle(getString(R.string.biometric_encrypt_secret_data))
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.use_password))
                .build()

    private val decryptPromptInfo
        get() = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_decrypt))
                .setSubtitle(getString(R.string.biometric_decrypt_secret_data))
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.use_password))
                .build()

}