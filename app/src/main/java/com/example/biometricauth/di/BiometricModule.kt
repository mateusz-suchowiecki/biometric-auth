package com.example.biometricauth.di

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
class BiometricModule {

    @Provides
    fun provideBiometricManager(@ApplicationContext context: Context) = BiometricManager.from(context)
}