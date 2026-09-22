package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {
    private const val TAG = "FirebaseAuthManager"

    @Volatile
    private var initialized = false

    fun getFirebaseAuth(context: Context): FirebaseAuth {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    initFirebase(context.applicationContext)
                    initialized = true
                }
            }
        }
        return FirebaseAuth.getInstance()
    }

    private fun initFirebase(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val defaultApp = try {
                FirebaseApp.initializeApp(context)
            } catch (e: Exception) {
                Log.w(TAG, "Standard Firebase initialization threw exception: ${e.message}")
                null
            }

            if (defaultApp == null && FirebaseApp.getApps(context).isEmpty()) {
                Log.d(TAG, "Standard Firebase options not found, initializing with gami-live-1d0ec project options.")
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:759875443958:android:770bc58aac9012d4cbe0d7")
                        .setApiKey("AIzaSyDX3vcR2YQzI--aowxB_CrUbNfPF79zplo")
                        .setProjectId("gami-live-1d0ec")
                        .setStorageBucket("gami-live-1d0ec.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.d(TAG, "FirebaseApp initialized with gami-live-1d0ec options.")
                } catch (fallbackEx: Exception) {
                    Log.e(TAG, "Fallback FirebaseApp initialization failed: ${fallbackEx.message}", fallbackEx)
                }
            } else {
                Log.d(TAG, "Firebase initialized via standard configuration.")
            }
        }
    }
}
