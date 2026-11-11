package ru.albertabdullin.todooshka.presentation.model

import java.time.LocalDate

data class SubtaskUi(
    val id: Int,
    val taskId: Int,
    val description: String,
    val descriptionVersion: Int = 0,
    val completedOn: LocalDate?,
    val sort: Int,
    val selectionPosition: Int,
    ) {
    val isCompleted: Boolean
        get() = completedOn != null
}
