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
        taskDescription: List<String>
    ): SubmitTaskResult {
        if (taskDescription.isBlank()) return NoTaskAdded
        //при попытке добавить новую задачу в середину списка
        if (submittedTaskPosition < currentTasksList.size - 1 &&
            currentTasksList[submittedTaskPosition + 1] is NewTask
        ) return NoTaskAdded

        val maxId = currentTasksList.maxOf { task -> task.id }
        val newTaskPosition = submittedTaskPosition + 1
        val newTaskId = maxId + 1
        val newTasksList = currentTasksList.toMutableList().apply {
            add(newTaskPosition, NewTask(id = newTaskId))
        }
        return TaskAdded(newTasksList = newTasksList, newTaskId = newTaskId)
    }


}

sealed interface SubmitTaskResult

data class TaskAdded(
    val newTasksList: List<Task>, val newTaskId: Int
) : SubmitTaskResult

data object NoTaskAdded : SubmitTaskResult