package com.example.biometricauth.di

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class Crypto @Inject constructor() {

    private var iv: ByteArray? = null

    fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(key: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey(key, null) as SecretKey
    }

    private fun getCipher() = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
            + KeyProperties.BLOCK_MODE_CBC + "/"
            + KeyProperties.ENCRYPTION_PADDING_PKCS7)

    fun cipherInEncryptMode(key: String): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey(key)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        iv = cipher.iv
        return cipher
    }

    fun cipherInDecryptMode(key: String): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey(key)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher
    }
}