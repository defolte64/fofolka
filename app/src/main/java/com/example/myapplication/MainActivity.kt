package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.utils.SharedPreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferencesManager.init(applicationContext)

        setContent {
            AppNavigation()
        }
    }
}
