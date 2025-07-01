package com.example.financeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {
    private var keepSplashOnScreen = true
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)


        // Keep the splash screen on until the animation completes
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // For Android versions below 12, handle the splash screen manually
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Handler(Looper.getMainLooper()).postDelayed({
                keepSplashOnScreen = false
            }, 2000) // Show splash for 2 seconds
        } else {
            // For Android 12+, keep the splash screen for the animation duration (600ms) plus a small buffer
            Handler(Looper.getMainLooper()).postDelayed({
                keepSplashOnScreen = false
            }, 800) // 600ms animation + 200ms buffer
        }

        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)
        bottomNavView.setOnItemSelectedListener { item ->
            // Trigger vibration
            triggerVibration()

            // Handle navigation using NavigationUI
            NavigationUI.onNavDestinationSelected(item, navController)
            true // Return true to indicate the item selection was handled
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Finance Alerts"
            val descriptionText = "Notifications for budget alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("finance_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For API 26 and above, use VibrationEffect
                val effect = VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                // For older APIs, use deprecated vibrate method
                @Suppress("DEPRECATION")
                vibrator.vibrate(5)
            }
        }
    }
}