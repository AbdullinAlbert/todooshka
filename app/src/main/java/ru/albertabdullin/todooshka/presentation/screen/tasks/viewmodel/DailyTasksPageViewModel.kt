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
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase
import java.time.LocalDate

class DailyTasksPageViewModel(
    private val tasksUseCase: TasksUseCase,
    private val dateForPage: LocalDate,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _tasksList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _tasksList

    private val defaultActiveTaskId = -1
    private var activeTaskId: Int = defaultActiveTaskId

    init {
        viewModelScope.launch {
            val list = tasksUseCase.getTasks(dateForPage)
            _tasksList.tryEmit(list)
        }
    }

    fun getActiveTaskPosition(): Int {
        return _tasksList.value.indexOfFirst { task -> task.id == activeTaskId }
    }

    fun isActiveTask(task: Task): Boolean {
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
            _tasksList.value,
            taskId,
            taskDescriptionPart1,
            taskDescriptionPart2
        )
        when (result) {
            is TaskAdded -> {
                viewModelScope.launch {
                    activeTaskId = result.newTaskId
                    _tasksList.tryEmit(result.newTasksList)
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
}