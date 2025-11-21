package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    fun saveUser(userId: String, email: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): String {
        return prefs.getString("user_id", "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun logout() {
        prefs.edit().apply {
            remove("user_id")
            remove("user_email")
            putBoolean("is_logged_in", false)
            apply()
        }
    }
}
