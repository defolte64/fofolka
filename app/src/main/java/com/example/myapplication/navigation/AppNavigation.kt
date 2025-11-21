package com.example.myapplication.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screen.LoginScreen
import com.example.myapplication.ui.screen.SignUpScreen
import com.example.myapplication.ui.screen.HabitListScreen
import com.example.myapplication.ui.screen.AddHabitScreen
import com.example.myapplication.ui.screen.HabitDetailScreen
import com.example.myapplication.utils.SharedPreferencesManager
import com.example.myapplication.network.SupabaseClient

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        SharedPreferencesManager.init(context)
        if (SharedPreferencesManager.isLoggedIn()) {
            val userId = SharedPreferencesManager.getUserId()
            val email = SharedPreferencesManager.getUserEmail()

            SupabaseClient.currentUserId = userId
            SupabaseClient.currentEmail = email

            startDestination = "habit_list"
        }
    }

    NavHost(navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("habit_list") { HabitListScreen(navController) }
        composable("add_habit") { AddHabitScreen(navController) }
        composable("habit_detail/{habitId}/{habitName}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
            val habitName = backStackEntry.arguments?.getString("habitName") ?: ""
            HabitDetailScreen(navController, habitId, habitName)
        }
    }
}
