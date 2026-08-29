package ru.albertabdullin.todooshka.presentation.dialog.datepicker.model

import java.time.LocalDate

data class AvailableDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)