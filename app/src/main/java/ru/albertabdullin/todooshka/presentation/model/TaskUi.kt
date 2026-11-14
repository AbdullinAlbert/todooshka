package ru.albertabdullin.todooshka.presentation.model

import java.time.LocalDate

data class TaskUi(
    val id: Int,
    val startOn: LocalDate,
    val durationDays: Int,
    val completedOn: LocalDate?,
    val description: String,
    val descriptionVersion: Int = 0,
    val sort: Int,
    val selectionPosition: Int,
    val subtasks: List<SubtaskUi>,
) {
    val isGroupTask: Boolean
        get() = subtasks.isNotEmpty()

    val isCompleted: Boolean
        get() = completedOn != null

    companion object {
        fun empty(startOn: LocalDate): TaskUi {
            return TaskUi(
                id = 0,
                startOn = startOn,
                durationDays = 1,
                completedOn = null,
                description = "",
                sort = 0,
                selectionPosition = 0,
                subtasks = emptyList()
            )
        }
    }
}

