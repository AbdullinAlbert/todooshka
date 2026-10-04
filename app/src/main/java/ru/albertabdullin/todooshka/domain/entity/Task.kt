package ru.albertabdullin.todooshka.domain.entity

data class Task(
    val id: Int,
    val description: String = "",
    val isCompleted: Boolean = false,
    val subTasks: List<Task> = emptyList()
)