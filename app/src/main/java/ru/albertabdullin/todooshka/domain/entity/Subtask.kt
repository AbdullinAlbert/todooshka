package ru.albertabdullin.todooshka.domain.entity

import java.time.LocalDate

data class Subtask(
    val id: Int,
    val taskId: Int,
    val description: String,
    val completedOn: LocalDate?,
    val sort: Int
)
