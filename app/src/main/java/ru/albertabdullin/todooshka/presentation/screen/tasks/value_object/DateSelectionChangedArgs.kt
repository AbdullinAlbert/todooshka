package ru.albertabdullin.todooshka.presentation.screen.tasks.value_object

data class DateSelectionChangedArgs(
    val previousSelectedPos: Int = Int.MIN_VALUE,
    val currentSelectedPos: Int = Int.MIN_VALUE,
    val previousSelectedCurrentSelectedDatesTextsRangeList: List<String>
) {
    val isDefault: Boolean
        get() = previousSelectedPos == currentSelectedPos
}