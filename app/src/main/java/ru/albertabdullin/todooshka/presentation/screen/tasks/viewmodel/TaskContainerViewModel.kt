package ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class TaskContainerViewModel(
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

    fun setSelectedDate(selectedDate: LocalDate) {
        if (selectedDate.toEpochDay() == selectedDateEpochDay.value) return
        savedStateHandle[SELECTED_DATE_KEY] = selectedDate.toEpochDay()
    }

    fun isSelectedDate(localDate: LocalDate): Boolean {
        return localDate.isEqual(LocalDate.ofEpochDay(selectedDateEpochDay.value))
    }

    private companion object {
        const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"
    }

}