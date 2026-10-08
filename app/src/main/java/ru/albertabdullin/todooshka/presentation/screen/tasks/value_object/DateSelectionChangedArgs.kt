package ru.albertabdullin.todooshka.presentation.screen.tasks.value_object

data class DateSelectionChangedArgs(
    val previousSelectedDayEpoch: Long = Long.MIN_VALUE,
    val currentSelectedDayEpoch: Long = Long.MIN_VALUE,
) {
    val isDefault: Boolean
        get() = previousSelectedDayEpoch == currentSelectedDayEpoch
}