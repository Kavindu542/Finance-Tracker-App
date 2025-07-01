package com.example.financeapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    var budget: Float = 0f
        set(value) {
            field = value
            saveSettings()
        }

    var currency: String = "LKR"
        set(value) {
            field = value
            saveSettings()
        }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val sharedPref = getApplication<Application>().getSharedPreferences("finance", 0)
        budget = sharedPref.getFloat("budget", 0f)
        currency = sharedPref.getString("currency", "LKR") ?: "LKR"
    }

    private fun saveSettings() {
        val sharedPref = getApplication<Application>().getSharedPreferences("finance", 0)
        val editor = sharedPref.edit()
        editor.putFloat("budget", budget)
        editor.putString("currency", currency)
        editor.apply()
    }
}