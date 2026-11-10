package ru.albertabdullin.todooshka.domain.entity

import java.time.LocalDate

data class Task(
    val id: Int,
    val description: String,
    val startOn: LocalDate,
    val completedOn: LocalDate?,
    val durationDays: Int,
    val sort: Int,
    val subtasks: List<Subtask>
)