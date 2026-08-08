package ru.albertabdullin.todooshka.domain.date_operations

sealed interface DateRangeChange {

    data object None : DateRangeChange

    data class InsertedAtStart(
        val count: Int,
    ) : DateRangeChange

    data class RemovedFromStart(
        val count: Int,
    ) : DateRangeChange
}