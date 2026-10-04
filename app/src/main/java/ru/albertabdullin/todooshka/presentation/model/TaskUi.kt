package ru.albertabdullin.todooshka.presentation.model

data class TaskUi(
    val id: Int,
    val description: String,
    val isCompleted: Boolean,
    val subTasks: List<TaskUi>,
    val isCompleteEnabled: Boolean,
    val selectionPosition: Int
) {
    val isGroupTask: Boolean
        get() = subTasks.isNotEmpty()
}

