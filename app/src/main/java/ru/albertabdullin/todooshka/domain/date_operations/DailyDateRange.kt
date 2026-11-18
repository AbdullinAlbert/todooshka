package ru.albertabdullin.todooshka.domain.date_operations

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DailyDateRange(
    val previousFirstDate: LocalDate,
    val currentFirstDate: LocalDate,
    private val lastDate: LocalDate
) {

    val size: Int = ChronoUnit.DAYS.between(currentFirstDate, lastDate).toInt() + 1

    fun dateAt(position: Int): LocalDate {
        return currentFirstDate.plusDays(position.toLong())
    }

    fun positionOf(date: LocalDate): Int {
        return ChronoUnit.DAYS.between(currentFirstDate, date).toInt()
    }

    fun positionOf(dateEpoch: Long): Int {
        val date = LocalDate.ofEpochDay(dateEpoch)
        return ChronoUnit.DAYS.between(currentFirstDate, date).toInt()
    }


    fun getDateRangeChange(): DateRangeChange {
        require(currentFirstDate.isBefore(lastDate))

        if (previousFirstDate == currentFirstDate) {
            return DateRangeChange.None
        }

        return if (currentFirstDate.isBefore(previousFirstDate)) {
            DateRangeChange.InsertedAtStart(
                count = ChronoUnit.DAYS.between(currentFirstDate, previousFirstDate).toInt()
            )
        } else {
            DateRangeChange.RemovedFromStart(
                count = ChronoUnit.DAYS.between(previousFirstDate, currentFirstDate).toInt()
            )
        }
    }
}