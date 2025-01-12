package com.inventory.tfgproject

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        initializeAppCheck()
    }

    private fun initializeAppCheck() {
        try {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            Log.d("AppCheck", "Iniciando configuración de AppCheck")

            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
            Log.d("AppCheck", "AppCheck configurado exitosamente")

        } catch (e: Exception) {
            Log.e("AppCheck", "Error initializing App Check", e)
            e.printStackTrace()
        }
    }
}