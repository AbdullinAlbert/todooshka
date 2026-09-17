package ru.albertabdullin.todooshka.presentation.screen.tasks.value_object

data class DateSelectionChangedArgs(
    val previousSelectedDayEpoch: Long,
    val currentSelectedDayEpoch: Long,
)