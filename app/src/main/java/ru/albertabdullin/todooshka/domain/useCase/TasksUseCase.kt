package ru.albertabdullin.todooshka.domain.useCase

import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import java.time.LocalDate

class TasksUseCase(
    private val taskRepository: TaskRepository
) {
    suspend fun getTasks(selectedDate: LocalDate): List<Task> {
        val tasks = taskRepository.getTasks(selectedDate)
        if (tasks.isNotEmpty()) return tasks
        return listOf(Task(id = 0))
    }

    fun deleteTask(
        currentTasksList: List<Task>,
        deletedTaskId: Int,
        deletedTaskDescription: String
    ): DeleteTaskResult {
        if (currentTasksList.size == 1) return NoTaskDeleted
        val tempList = currentTasksList.toMutableList()
        val deletedTaskIndex = tempList.indexOfFirst { task -> task.id == deletedTaskId }
        if (deletedTaskIndex == -1) return NoTaskDeleted
        if (deletedTaskIndex == 0 && deletedTaskDescription.isNotBlank()) return NoTaskDeleted
        val activeTaskIndex =
            if (deletedTaskIndex == 0) 1 else deletedTaskIndex - 1
        val newActiveTask = tempList[activeTaskIndex]
        tempList[activeTaskIndex] =
            newActiveTask.copy(description = newActiveTask.description + deletedTaskDescription)
        val selectionPos = if (deletedTaskIndex == 0) 0 else newActiveTask.description.length
        val newTaskList = tempList.filter { task -> task.id != deletedTaskId }
        return TaskDeleted(
            activeTaskId = newActiveTask.id,
            newTasksList = newTaskList,
            selectionPos = selectionPos
        )
    }

    fun submitTaskAndCreateNewOne(
        currentTasksList: List<Task>,
        submittedTaskId: Int,
        submittedTaskDescriptionPart1: String,
        submittedTaskDescriptionPart2: String,
    ): SubmitTaskResult {
        //если текущая строка пустая, то ничего не добавляем
        if (submittedTaskDescriptionPart1.isBlank() && submittedTaskDescriptionPart2.isBlank()) return NoTaskAdded

        val newTaskId = currentTasksList.maxOf { task -> task.id } + 1
        val submittedTaskPosition =
            currentTasksList.indexOfFirst { task -> task.id == submittedTaskId }

        if (submittedTaskDescriptionPart1.isBlank()) {
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
            val canAddTask = submittedTaskPosition == currentTasksList.lastIndex ||
                    submittedTaskDescriptionPart2.isNotBlank() || (currentTasksList[submittedTaskPosition + 1].description.isNotBlank())
            return if (canAddTask) {
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
        val newTask = Task(id = newTaskId, description = newTaskDescription)
        val tempTasksList = currentTasksList.toMutableList()
        val currentSubmittedTask = tempTasksList[submittedTaskPosition]
        val updatedSubmittedTask = currentSubmittedTask.copy(description = currentTaskDescription)
        tempTasksList[submittedTaskPosition] = updatedSubmittedTask
        tempTasksList.add(newTaskPosition, newTask)
        return TaskAdded(newTasksList = tempTasksList, newTaskId)
    }

}

sealed interface SubmitTaskResult

data class TaskAdded(
    val newTasksList: List<Task>, val newTaskId: Int
) : SubmitTaskResult

data object NoTaskAdded : SubmitTaskResult

sealed interface DeleteTaskResult

data class TaskDeleted(
    val newTasksList: List<Task>,
    val activeTaskId: Int,
    val selectionPos: Int
) : DeleteTaskResult

data object NoTaskDeleted : DeleteTaskResult