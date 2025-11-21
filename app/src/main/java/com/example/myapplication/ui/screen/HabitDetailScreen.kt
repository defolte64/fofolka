package com.example.myapplication.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.network.SupabaseClient
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitDetailScreen(navController: NavController, habitId: Int, habitName: String) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val userId = SupabaseClient.currentUserId

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFFbccdfa), Color(0xFF7F52FD))
    )

    LaunchedEffect(Unit) {
        isLoading = true
        SupabaseClient.getHabits(userId) { habits, _ ->
            habits?.find { it.id == habitId }?.let { habit ->
                selectedDates = habit.checkedDates.mapNotNull { dateStr ->
                    try {
                        LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
                    } catch (e: Exception) {
                        null
                    }
                }.toSet()
            }
            isLoading = false
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.width(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Text("←", fontSize = 20.sp)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = habitName,
                        style = MaterialTheme.typography.h6,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        "Выполнено: ${selectedDates.size} раз",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(50.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier
                            .width(50.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF7F52FD),
                            contentColor = Color.White
                        )
                    ) {
                        Text("<", fontSize = 18.sp)
                    }

                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.h6,
                        fontSize = 16.sp,
                        color = Color(0xFF7F52FD),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier
                            .width(50.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF7F52FD),
                            contentColor = Color.White
                        )
                    ) {
                        Text(">", fontSize = 18.sp)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color(0xFF7F52FD)
                            )
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF7F52FD))
                        }
                    } else {
                        val firstDay = currentMonth.atDay(1)
                        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
                        val daysInMonth = currentMonth.lengthOfMonth()
                        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(totalCells) { index ->
                                val dayNum = index - firstDayOfWeek + 1
                                val date = if (dayNum in 1..daysInMonth) {
                                    currentMonth.atDay(dayNum)
                                } else {
                                    null
                                }

                                if (date != null) {
                                    val isSelected = date in selectedDates
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                            .background(
                                                color = if (isSelected) Color(0xFF7F52FD) else Color(0xFFEEF0F7),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedDates = if (isSelected) {
                                                    selectedDates - date
                                                } else {
                                                    selectedDates + date
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            color = if (isSelected) Color.White else Color(0xFF7F52FD),
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isSaving = true
                    val datesList = selectedDates
                        .sorted()
                        .map { it.format(DateTimeFormatter.ISO_DATE) }

                    SupabaseClient.updateHabitDates(habitId, userId, datesList) { success, error ->
                        isSaving = false
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF7F52FD)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Сохраняем..." else "Сохранить", fontSize = 16.sp)
            }
        }
    }
}
