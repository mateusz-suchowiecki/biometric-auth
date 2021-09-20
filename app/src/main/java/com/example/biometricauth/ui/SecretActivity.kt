package com.example.biometricauth.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.biometricauth.databinding.ActivitySecretBinding

class SecretActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecretBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }
}