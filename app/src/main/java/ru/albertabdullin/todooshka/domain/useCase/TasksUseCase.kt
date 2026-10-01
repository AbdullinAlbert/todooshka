package ru.albertabdullin.todooshka.domain.useCase

import ru.albertabdullin.todooshka.domain.entity.NewTask
import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import java.time.LocalDate

class TasksUseCase(
    private val taskRepository: TaskRepository
) {
    suspend fun getTasks(selectedDate: LocalDate): List<Task> {
        val tasks = taskRepository.getTasks(selectedDate)
        if (tasks.isNotEmpty()) return tasks
        return listOf(NewTask())
    }

    fun submitTaskAndCreateNewOne(
        currentTasksList: List<Task>,
        submittedTaskPosition: Int,
        submitTaskData: SubmitTaskData
    ): SubmitTaskResult {
        if (submitTaskData.taskDescriptionIsEmpty)
        val newId = currentTasksList.maxOf { task -> task.id } + 1
        val currentSumb
    }

}

data class SubmitTaskData(
    val taskDescriptionPart1: String,
    val taskDescriptionPart2: String
) {
    val taskDescriptionIsEmpty: Boolean
        get() = taskDescriptionPart1.isEmpty() && taskDescriptionPart2.isEmpty()
}

sealed interface SubmitTaskResult

data class TaskAdded(
    val newTasksList: List<Task>, val newTaskId: Int
) : SubmitTaskResult

data object NoTaskAdded : SubmitTaskResult