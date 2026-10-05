package ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.domain.entity.Task
import ru.albertabdullin.todooshka.domain.useCase.TaskAdded
import ru.albertabdullin.todooshka.domain.useCase.TaskDeleted
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase
import ru.albertabdullin.todooshka.presentation.model.TaskUi
import java.time.LocalDate

class DailyTasksPageViewModel(
    private val tasksUseCase: TasksUseCase,
    private val dateForPage: LocalDate,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _tasksList = MutableStateFlow<List<TaskUi>>(emptyList())
    val taskList: StateFlow<List<TaskUi>> = _tasksList

    private val defaultActiveTaskId = -1
    private var activeTaskId: Int = defaultActiveTaskId

    init {
        viewModelScope.launch {
            val list = tasksUseCase
                .getTasks(dateForPage)
                .map { it.toUi() }
            _tasksList.tryEmit(list)
        }
    }

    fun getActiveTaskPosition(): Int {
        return _tasksList.value.indexOfFirst { task -> task.id == activeTaskId }
    }

    fun isActiveTask(task: TaskUi): Boolean {
        val isActiveTask = task.id == activeTaskId
        if (isActiveTask) {
            activeTaskId = defaultActiveTaskId
        }
        return isActiveTask
    }

    fun onTaskSubmitted(
        taskId: Int,
        taskDescriptionPart1: String,
        taskDescriptionPart2: String
    ) {
        val result = tasksUseCase.submitTaskAndCreateNewOne(
            _tasksList.value.map { it.toDomain() },
            taskId,
            taskDescriptionPart1,
            taskDescriptionPart2
        )
        when (result) {
            is TaskAdded -> {
                viewModelScope.launch {
                    activeTaskId = result.newTaskId
                    _tasksList.tryEmit(result.newTasksList.map { it.toUi() })
                }
            }

            else -> Unit
        }
    }

    fun onTaskDescriptionChanged(taskId: Int, taskDescription: String) {
        val tempList = _tasksList.value.toMutableList()
        val taskIndex = tempList.indexOfFirst { task -> task.id == taskId }
        if (taskIndex == -1) return
        val updatedTask = tempList[taskIndex].copy(description = taskDescription)
        tempList[taskIndex] = updatedTask
        viewModelScope.launch {
            _tasksList.tryEmit(tempList)
        }
    }

    fun onTaskDeleted(taskId: Int, taskDescription: String) {
        val result = tasksUseCase.deleteTask(
            currentTasksList = _tasksList.value.map { it.toDomain() },
            deletedTaskId = taskId,
            deletedTaskDescription = taskDescription
        )
        when (result) {
            is TaskDeleted -> {
                viewModelScope.launch {
                    activeTaskId = result.activeTaskId
                    _tasksList.tryEmit(result.newTasksList.map {
                        val selectionPos =
                            if (it.id == result.activeTaskId) result.selectionPos else 0
                        it.toUi(selectionPos = selectionPos)
                    })
                }
            }

            else -> Unit
        }
    }

    companion object {
        fun factory(
            tasksUseCase: TasksUseCase, dateForPage: LocalDate
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DailyTasksPageViewModel(
                    tasksUseCase = tasksUseCase,
                    dateForPage = dateForPage,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }

    private fun Task.toUi(selectionPos: Int = 0, isCompleteEnabled: Boolean = true): TaskUi {
        return TaskUi(
            id = id,
            isCompleted = isCompleted,
            description = description,
            isCompleteEnabled = isCompleteEnabled,
            selectionPosition = selectionPos,
            subTasks = subTasks.map { it.toUi() }
        )
    }

    private fun TaskUi.toDomain(): Task {
        return Task(
            id = id,
            description = description,
            isCompleted = isCompleted,
            subTasks = subTasks.map { it.toDomain() }
        )
    }
}