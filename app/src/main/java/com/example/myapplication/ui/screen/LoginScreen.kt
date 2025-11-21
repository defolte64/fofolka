package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.network.SupabaseClient

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
                        text = "Вход",
                        style = MaterialTheme.typography.h6,
                        color = Color(0xFF7F52FD),
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFEEF0F7),
                            focusedIndicatorColor = Color(0xFF7F52FD),
                            unfocusedIndicatorColor = Color(0xFFD0D5E8),
                            textColor = Color.Black
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFEEF0F7),
                            focusedIndicatorColor = Color(0xFF7F52FD),
                            unfocusedIndicatorColor = Color(0xFFD0D5E8),
                            textColor = Color.Black
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            SupabaseClient.signIn(email, password) { success, error ->
                                isLoading = false
                                if (success) {
                                    navController.navigate("habit_list") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = error ?: "Ошибка авторизации"
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
                        enabled = !isLoading
                    ) {
                        Text(
                            if (isLoading) "Входим..." else "Войти",
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Нет аккаунта? Зарегистрируйтесь",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }
    }
}
