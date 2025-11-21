package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.myapplication.network.SupabaseClient
import kotlinx.coroutines.delay

@Composable
fun AddHabitScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val userId = SupabaseClient.currentUserId

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFFbccdfa), Color(0xFF7F52FD))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 12.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Добавить привычку",
                        style = MaterialTheme.typography.h6,
                        color = Color(0xFF7F52FD),
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("Название привычки") },
                        placeholder = { Text("Например: Отжаться 20 раз") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFEEF0F7),
                            focusedIndicatorColor = Color(0xFF7F52FD),
                            unfocusedIndicatorColor = Color(0xFFD0D5E8),
                            textColor = Color.Black
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null

                            if (name.isBlank()) {
                                errorMessage = "Введите название привычки"
                                return@Button
                            }

                            if (userId.isBlank()) {
                                errorMessage = "Ошибка: пользователь не авторизован"
                                return@Button
                            }

                            isLoading = true
                            SupabaseClient.addHabit(userId, name) { success, error ->
                                isLoading = false
                                if (success) {
                                    successMessage = "Привычка добавлена!"
                                    name = ""
                                    navController.popBackStack()
                                } else {
                                    errorMessage = error ?: "Ошибка добавления привычки"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF7F52FD),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && name.isNotBlank()
                    ) {
                        Text(
                            if (isLoading) "Сохраняем..." else "Сохранить",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(
                            errorMessage!!,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            fontSize = 13.sp
                        )
                    }

                    if (successMessage != null) {
                        Text(
                            successMessage!!,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White.copy(alpha = 0.3f),
                    contentColor = Color.White
                )
            ) {
                Text("Отменить", fontSize = 14.sp)
            }
        }
    }
}
