package com.example.myapplication.model

data class Habit(
    val id: Int,
    val name: String,
    val checkedDates: List<String> = emptyList()
)
