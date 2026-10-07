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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import ru.albertabdullin.todooshka.presentation.screen.tasks.value_object.DatePickerArgs
import ru.albertabdullin.todooshka.presentation.screen.tasks.value_object.DateSelectionChangedArgs
import java.time.LocalDate


class TaskContainerViewModel(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var firstDate = LocalDate.now()

    private val _taskContainerState = MutableStateFlow(TaskTrackerState())
    val taskContainerState: StateFlow<TaskTrackerState> = _taskContainerState

    init {
        viewModelScope.launch {
            taskRepository
                .getDateLeftBound()
                .collect { firstDateBound ->
                    firstDate = firstDateBound
                }
        }
    }

    private val _dateTabBottomCoordinate = MutableStateFlow<Int>(-1)
    val dateTabBottomCoordinate: StateFlow<Int> = _dateTabBottomCoordinate

    fun updateDateTabBottomCoordinate(dateBottomCoordinate: Int) {
        viewModelScope.launch {
            _dateTabBottomCoordinate.tryEmit(dateBottomCoordinate)
        }
    }

    private val selectedDateEpochDay
        get(): Long {
            var selectedDate = savedStateHandle[SELECTED_DATE_KEY] as? Long
            if (selectedDate == null) {
                selectedDate = LocalDate.now().toEpochDay()
                savedStateHandle[SELECTED_DATE_KEY] = selectedDate
            }
            return selectedDate
        }

    private val _scrollDateTabEvent = MutableSharedFlow<DateSelectionChangedArgs>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val scrollDateEvent: SharedFlow<DateSelectionChangedArgs> = _scrollDateTabEvent

    private val _openCalendarEvent = MutableSharedFlow<DatePickerArgs>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val openCalendarEvent: SharedFlow<DatePickerArgs> = _openCalendarEvent

    fun onNewDateIsSelected(selectedDate: LocalDate) {
        val (previous, current) = updateSelectedDate(selectedDate) ?: return
        _scrollDateTabEvent.tryEmit(
            DateSelectionChangedArgs(
                previous,
                current,
            )
        )
    }

    private fun updateSelectedDate(selectedDate: LocalDate): Pair<Long, Long>? {
        if (selectedDate.toEpochDay() == selectedDateEpochDay) return null
        val previousSelectedDayEpoch = selectedDateEpochDay
        val currentSelectedDayEpoch = selectedDate.toEpochDay()
        savedStateHandle[SELECTED_DATE_KEY] = currentSelectedDayEpoch
        return Pair(previousSelectedDayEpoch, currentSelectedDayEpoch)
    }

    fun isSelectedDate(localDate: LocalDate): Boolean {
        return localDate.isEqual(LocalDate.ofEpochDay(selectedDateEpochDay))
    }

    fun openCalendarDialogButtonIsClicked() {
        _openCalendarEvent.tryEmit(
            DatePickerArgs(
                firstDate = firstDate,
                selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
            )
        )
    }

    fun onTaskTrackerRepresentationChanged() {
        val newRepresentation = _taskContainerState.value.representationTaskTrackerMode.inverse()
        viewModelScope.launch {
            _taskContainerState.tryEmit(
                _taskContainerState.value.copy(representationTaskTrackerMode = newRepresentation)
            )
        }
    }

    fun onBackToReadMode() {
        viewModelScope.launch {
            _taskContainerState.tryEmit(
                _taskContainerState.value.copy(taskTrackerWorkMode = TaskTrackerWorkMode.READ)
            )
        }
    }

    fun onTaskDescriptionFocus(hasFocus: Boolean) {
        if (hasFocus) {
            viewModelScope.launch {
                _taskContainerState.tryEmit(
                    _taskContainerState.value.copy(taskTrackerWorkMode = TaskTrackerWorkMode.EDIT)
                )
            }
        }
    }

    companion object {
        private const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"

        fun factory(
            taskRepository: TaskRepository,
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

enum class TaskTrackerWorkMode {
    READ, EDIT, SEARCH,
}

enum class RepresentationTaskTrackerMode {
    DAILY, WEEKLY;

    fun inverse(): RepresentationTaskTrackerMode {
        return when (this) {
            DAILY -> WEEKLY
            WEEKLY -> DAILY
        }
    }
}

data class TaskTrackerState(
    val representationTaskTrackerMode: RepresentationTaskTrackerMode = RepresentationTaskTrackerMode.DAILY,
    val taskTrackerWorkMode: TaskTrackerWorkMode = TaskTrackerWorkMode.READ
)