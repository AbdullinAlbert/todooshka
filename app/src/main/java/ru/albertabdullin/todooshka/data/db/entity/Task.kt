package ru.albertabdullin.todooshka.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val id: Int
)
