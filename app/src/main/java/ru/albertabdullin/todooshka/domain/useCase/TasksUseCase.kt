package ru.albertabdullin.todooshka.domain.useCase

import ru.albertabdullin.todooshka.domain.entity.ComplexTask
import ru.albertabdullin.todooshka.domain.entity.NewTask
import ru.albertabdullin.todooshka.domain.entity.SimpleTask
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
        submittedTaskId: Int,
        submittedTaskDescriptionPart1: String,
        submittedTaskDescriptionPart2: String,
    ): SubmitTaskResult {
        //если текущая строка пустая, то ничего не добавляем
        if (submittedTaskDescriptionPart1.isEmpty() && submittedTaskDescriptionPart2.isEmpty()) return NoTaskAdded

        val newTaskId = currentTasksList.maxOf { task -> task.id } + 1
        val submittedTaskPosition = currentTasksList.indexOfFirst { task -> task.id == submittedTaskId }

        if (submittedTaskDescriptionPart1.isEmpty()) {
            //если курсор стоит перед первым символом
            return if (submittedTaskPosition == 0 || (currentTasksList[submittedTaskPosition - 1].description.isNotBlank())) {
                addTask(
                    currentTasksList = currentTasksList,
                    submittedTaskPosition = submittedTaskPosition,
                    currentTaskDescription = submittedTaskDescriptionPart2,
                    newTaskPosition = submittedTaskPosition,
                    newTaskId = newTaskId,
                    newTaskDescription = submittedTaskDescriptionPart1,
                )
            } else NoTaskAdded
        } else { //если курсор стоит в середине текста или в конце
            return if (submittedTaskPosition == currentTasksList.size - 1 || (currentTasksList[submittedTaskPosition + 1].description.isNotBlank())) {
                addTask(
                    currentTasksList = currentTasksList,
                    submittedTaskPosition = submittedTaskPosition,
                    currentTaskDescription = submittedTaskDescriptionPart1,
                    newTaskPosition = submittedTaskPosition + 1,
                    newTaskId = newTaskId,
                    newTaskDescription = submittedTaskDescriptionPart2,
                )
            } else NoTaskAdded
        }
    }

    private fun addTask(
        currentTasksList: List<Task>,
        submittedTaskPosition: Int,
        currentTaskDescription: String,
        newTaskPosition: Int,
        newTaskId: Int,
        newTaskDescription: String,
    ): TaskAdded {
        val newTask = NewTask(id = newTaskId, description = newTaskDescription)
        val tempTasksList = currentTasksList.toMutableList()
        val currentSubmittedTask = tempTasksList[submittedTaskPosition]
        val updatedSubmittedTask =
            currentSubmittedTask.copy(newDescription = currentTaskDescription)
        tempTasksList[submittedTaskPosition] = updatedSubmittedTask
        tempTasksList.add(newTaskPosition, newTask)
        return TaskAdded(newTasksList = tempTasksList, newTaskId)
    }

}

fun Task.copy(
    newDescription: String? = null,
    newIsCompleted: Boolean? = null
): Task {
    return when (this) {
        is NewTask -> copy(
            description = newDescription ?: description,
            isCompleted = newIsCompleted ?: isCompleted
        )

        is SimpleTask -> copy(
            description = newDescription ?: description,
            isCompleted = newIsCompleted ?: isCompleted
        )

        is ComplexTask -> copy(
            description = newDescription ?: description,
            isCompleted = newIsCompleted ?: isCompleted
        )
    }
}

sealed interface SubmitTaskResult

data class TaskAdded(
    val newTasksList: List<Task>, val newTaskId: Int
) : SubmitTaskResult

data object NoTaskAdded : SubmitTaskResult