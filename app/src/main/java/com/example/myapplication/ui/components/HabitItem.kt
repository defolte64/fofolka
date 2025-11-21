package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.model.Habit

@Composable
fun HabitItem(
    habit: Habit,
    navController: NavController? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.body1,
                    fontSize = 16.sp,
                    color = Color(0xFF7F52FD)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Выполнено: ${habit.checkedDates.size} раз",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = {
                    navController?.navigate("habit_detail/${habit.id}/${habit.name}")
                },
                modifier = Modifier
                    .width(48.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF7F52FD),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📅", fontSize = 16.sp)
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { onDelete?.invoke() },
                modifier = Modifier
                    .width(48.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🗑", fontSize = 16.sp)
            }
        }
    }
}
