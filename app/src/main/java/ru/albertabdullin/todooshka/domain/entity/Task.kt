package ru.albertabdullin.todooshka.domain.entity

sealed class Task {
    abstract val id: Long
    abstract val description: String
    abstract val isCompleted: Boolean
}

data class NewTask(
    override val id: Long = -1,
    override val description: String = "",
    override val isCompleted: Boolean = false
) : Task()

data class SimpleTask(
    override val id: Long,
    override val description: String,
    override val isCompleted: Boolean
) : Task()

data class ComplexTask(
    override val id: Long,
    override val description: String,
    override val isCompleted: Boolean,
    val subtasks: List<SimpleTask>
) : Task()