package ru.albertabdullin.todooshka.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val description: String,
    val completedOn: LocalDate?,
    val startOn: LocalDate,
    val durationDays: Int,
    val sort: Int
)
