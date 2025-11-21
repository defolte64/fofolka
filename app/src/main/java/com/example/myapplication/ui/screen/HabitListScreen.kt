package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.model.Habit
import com.example.myapplication.network.SupabaseClient
import com.example.myapplication.ui.components.HabitItem

@Composable
fun HabitListScreen(navController: NavController) {
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val userId = SupabaseClient.currentUserId

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFFbccdfa), Color(0xFF7F52FD))
    )

    LaunchedEffect(refreshTrigger) {
        if (userId.isNotBlank()) {
            isLoading = true
            error = null
            SupabaseClient.getHabits(userId) { loadedHabits, err ->
                if (loadedHabits != null) {
                    habits = loadedHabits
                    error = null
                } else {
                    error = err ?: "Ошибка загрузки привычек"
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val callback = navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.route == "habit_list") {
                refreshTrigger++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Ваши привычки",
                style = MaterialTheme.typography.h5,
                color = Color.White,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (habits.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(habits) { habit ->
                        HabitItem(
                            habit,
                            navController,
                            onDelete = {
                                SupabaseClient.deleteHabit(habit.id, userId) { success, err ->
                                    if (success) {
                                        refreshTrigger++
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Нет привычек", color = Color.White, fontSize = 16.sp)
                }
            }

            if (error != null) {
                Text(error!!, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
            }

            Button(
                onClick = { navController.navigate("add_habit") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF7F52FD)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Добавить привычку", fontSize = 16.sp)
            }

            Button(
                onClick = {
                    SupabaseClient.signOut { _ ->
                        navController.navigate("login") {
                            popUpTo("habit_list") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White.copy(alpha = 0.3f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Выход", fontSize = 16.sp)
            }
        }
    }
}
