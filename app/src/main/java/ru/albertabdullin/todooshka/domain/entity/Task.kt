package ru.albertabdullin.todooshka.domain.entity

data class Task(
    val id: Long,
    val parentId: Long? = null,
    val description: String,
    val isCompleted: Boolean
)
