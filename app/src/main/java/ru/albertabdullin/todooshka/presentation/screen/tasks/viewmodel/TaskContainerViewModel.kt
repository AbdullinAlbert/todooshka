package ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import java.time.LocalDate


class TaskContainerViewModel(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selectedDateEpochDay = savedStateHandle.getStateFlow(
        key = SELECTED_DATE_KEY,
        initialValue = LocalDate.now().toEpochDay()
    )

    val selectedDate: StateFlow<LocalDate> =
        selectedDateEpochDay
            .map(LocalDate::ofEpochDay)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = LocalDate.ofEpochDay(
                    selectedDateEpochDay.value
                )
            )

    private val _scrollDateTabEvent = MutableSharedFlow<LocalDate>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val scrollDateTabEvent: SharedFlow<LocalDate> = _scrollDateTabEvent

    fun setSelectedDate(selectedDate: LocalDate) {
        if (selectedDate.toEpochDay() == selectedDateEpochDay.value) return
        savedStateHandle[SELECTED_DATE_KEY] = selectedDate.toEpochDay()
        _scrollDateTabEvent.tryEmit(selectedDate)
    }

    fun isSelectedDate(localDate: LocalDate): Boolean {
        return localDate.isEqual(LocalDate.ofEpochDay(selectedDateEpochDay.value))
    }

    companion object {
        private const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"

        fun factory(
            taskRepository: TaskRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TaskContainerViewModel(
                    taskRepository = taskRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }

}