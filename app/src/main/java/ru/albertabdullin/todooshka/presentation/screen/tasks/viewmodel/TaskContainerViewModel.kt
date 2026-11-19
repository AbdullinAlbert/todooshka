package ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.repository.TaskRepository
import ru.albertabdullin.todooshka.presentation.screen.tasks.value_object.DatePickerArgs
import ru.albertabdullin.todooshka.presentation.screen.tasks.value_object.DateSelectionChangedArgs
import java.time.LocalDate


class TaskContainerViewModel(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _tasksDateRange = MutableStateFlow(DailyDateRange())
    val tasksDateRange: Flow<DailyDateRange> = _tasksDateRange.filter { !it.isDefault }

    private val _dailyDatesDataContext = MutableStateFlow(DailyDatesDataContext())
    val dailyDatesDataContext: Flow<DailyDatesDataContext> = _dailyDatesDataContext.filter { !it.isDefault }

    private val _taskTrackerMode = MutableStateFlow(TaskTrackerMode())
    val taskTrackerMode: StateFlow<TaskTrackerMode> = _taskTrackerMode

    private val _openCalendarEvent = MutableSharedFlow<DatePickerArgs>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val openCalendarEvent: SharedFlow<DatePickerArgs> = _openCalendarEvent

    private val _appBarItemMenuEvent = MutableSharedFlow<AppBarItemMenuEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val appBarItemMenuEvent: SharedFlow<AppBarItemMenuEvent> = _appBarItemMenuEvent

    private val _dateTabBottomCoordinate = MutableStateFlow(-1)
    val dateTabBottomCoordinate: StateFlow<Int> = _dateTabBottomCoordinate

    init {
        viewModelScope.launch {
            taskRepository
                .getTasksDateLeftBound()
                .collect { firstDateBound ->
                    val previousFirstDate = _tasksDateRange.value.currentFirstDate
                    _tasksDateRange.emit(
                        _tasksDateRange.value.copy(
                            previousFirstDate = previousFirstDate,
                            currentFirstDate = firstDateBound
                        )
                    )
                }
        }
    }

    fun updateDateTabBottomCoordinate(dateBottomCoordinate: Int) {
        viewModelScope.launch {
            _dateTabBottomCoordinate.emit(dateBottomCoordinate)
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

    fun onNewDateIsSelected(selectedDate: LocalDate) {
        val (previousPos, currentPos) = updateSelectedDate(selectedDate) ?: return
        viewModelScope.launch {
            _dailyDatesDataContext.emit(
                _dailyDatesDataContext.value.copy(
                    dateSelectionChangedArgs = DateSelectionChangedArgs(
                        previousPos,
                        currentPos,
                    ),
                    dateRange = _tasksDateRange.value
                )
            )
        }
    }

    private fun updateSelectedDate(selectedDate: LocalDate): Pair<Int, Int>? {
        Log.d(TaskContainerViewModel::class.simpleName, "updateSelectedDate selectedDate = $selectedDate")

        if (selectedDate.toEpochDay() == selectedDateEpochDay) return null
        val previousSelectedDayEpoch = selectedDateEpochDay
        val currentSelectedDayEpoch = selectedDate.toEpochDay()
        savedStateHandle[SELECTED_DATE_KEY] = currentSelectedDayEpoch
        return Pair(
            _tasksDateRange.value.positionOf(previousSelectedDayEpoch),
            _tasksDateRange.value.positionOf(currentSelectedDayEpoch)
        )
    }

    fun isSelectedDate(localDate: LocalDate): Boolean {
        val selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
        val res = localDate.isEqual(selectedDate)
        Log.d(TaskContainerViewModel::class.simpleName, "localDate = $localDate, selectedDate = $selectedDate, res = $res")
        return res
    }

    fun openCalendarDialogButtonIsClicked() {
        viewModelScope.launch {
            _openCalendarEvent.emit(
                DatePickerArgs(
                    firstDate = _tasksDateRange.value.currentFirstDate,
                    lastDate = _tasksDateRange.value.lastDate,
                    selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
                )
            )
        }
    }

    fun onTaskTrackerRepresentationChanged() {
        val newRepresentation = _taskTrackerMode.value.representationTaskTrackerMode.inverse()
        viewModelScope.launch {
            _taskTrackerMode.emit(
                _taskTrackerMode.value.copy(representationTaskTrackerMode = newRepresentation)
            )
        }
    }

    fun onBackToReadModeButtonWasClicked() {
        viewModelScope.launch {
            _appBarItemMenuEvent.emit(BackButtonIsClicked)
        }
    }

    fun onSaveButtonWasClicked() {
        viewModelScope.launch {
            _appBarItemMenuEvent.emit(SaveButtonIsClicked)
        }
    }

    fun onTasksSaved() {
        viewModelScope.launch {
            _taskTrackerMode.emit(
                _taskTrackerMode.value.copy(taskTrackerWorkMode = TaskTrackerWorkMode.READ)
            )
        }
    }

    fun onTaskDescriptionFocus(hasFocus: Boolean) {
        if (hasFocus) {
            viewModelScope.launch {
                _taskTrackerMode.emit(
                    _taskTrackerMode.value.copy(taskTrackerWorkMode = TaskTrackerWorkMode.EDIT)
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

data class TaskTrackerMode(
    val representationTaskTrackerMode: RepresentationTaskTrackerMode = RepresentationTaskTrackerMode.DAILY,
    val taskTrackerWorkMode: TaskTrackerWorkMode = TaskTrackerWorkMode.READ
)

data class DailyDatesDataContext(
    val dateSelectionChangedArgs: DateSelectionChangedArgs = DateSelectionChangedArgs(),
    val dateRange: DailyDateRange = DailyDateRange()
) {
    val isDefault: Boolean
        get() = dateSelectionChangedArgs.isDefault || dateRange.isDefault
}

sealed interface AppBarItemMenuEvent

data object BackButtonIsClicked : AppBarItemMenuEvent
data object SaveButtonIsClicked : AppBarItemMenuEvent