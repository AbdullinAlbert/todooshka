package ru.albertabdullin.todooshka.domain.date_operations

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DailyDateRange(private var firstDate: LocalDate, private val lastDate: LocalDate) {

    val size: Int = ChronoUnit.DAYS.between(firstDate, lastDate).toInt() + 1

    fun dateAt(position: Int): LocalDate {
        return firstDate.plusDays(position.toLong())
    }

    fun positionOf(date: LocalDate): Int {
        return ChronoUnit.DAYS.between(firstDate, date).toInt()
    }


    fun updateFirstDate(newDate: LocalDate): DateRangeChange {
        require(newDate.isBefore(lastDate))

        val previousDate = firstDate

        if (newDate == previousDate) {
            return DateRangeChange.None
        }

        firstDate = newDate

        return if (newDate.isBefore(previousDate)) {
            DateRangeChange.InsertedAtStart(
                count = ChronoUnit.DAYS.between(newDate, previousDate).toInt()
            )
        } else {
            DateRangeChange.RemovedFromStart(
                count = ChronoUnit.DAYS.between(previousDate, newDate).toInt()
            )
        }
    }
}