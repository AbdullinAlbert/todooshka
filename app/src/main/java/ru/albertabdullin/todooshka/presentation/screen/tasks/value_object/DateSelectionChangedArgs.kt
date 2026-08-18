package ru.albertabdullin.todooshka.presentation.screen.tasks.value_object

data class DateSelectionChangedArgs(
    val previousSelectedDayEpoch: Long,
    val currentSelectedDayEpoch: Long,
    val selectionDateSource: SelectionDateSource
) {
    enum class SelectionDateSource {
        DATE_TAB, CALENDAR, VIEW_PAGER
    }
}