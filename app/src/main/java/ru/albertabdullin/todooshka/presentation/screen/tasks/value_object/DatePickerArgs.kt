package ru.albertabdullin.todooshka.presentation.screen.tasks.value_object

import java.time.LocalDate

data class DatePickerArgs(
    val firstDate: LocalDate,
    val selectedDate: LocalDate
)
