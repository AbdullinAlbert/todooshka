package ru.albertabdullin.todooshka.data.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.ForeignKey.Companion.CASCADE
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "subtasks",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["taskId"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val taskId: Int,
    val description: String,
    val completedOn: LocalDate?,
    val sort: Int
)
