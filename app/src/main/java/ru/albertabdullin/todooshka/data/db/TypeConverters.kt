package ru.albertabdullin.todooshka.data.db

import androidx.room3.ColumnTypeConverter
import java.time.LocalDate

object TypeConverters {
    @ColumnTypeConverter
    fun fromStringToLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @ColumnTypeConverter
    fun fromLocalDateToString(value: LocalDate?): String? {
        return value?.toString()
    }
}