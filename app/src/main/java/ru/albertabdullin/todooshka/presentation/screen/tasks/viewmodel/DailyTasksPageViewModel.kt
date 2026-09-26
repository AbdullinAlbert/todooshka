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
import ru.albertabdullin.todooshka.domain.useCase.TasksUseCase
import java.time.LocalDate

class DailyTasksPageViewModel(
    private val tasksUseCase: TasksUseCase,
    private val dateForPage: LocalDate,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _tasksList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _tasksList

    init {
        viewModelScope.launch {
            val list = tasksUseCase.getTasks(dateForPage)
            _tasksList.tryEmit(list)
        }
    }

    companion object {
        fun factory(
            tasksUseCase: TasksUseCase,
            dateForPage: LocalDate
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