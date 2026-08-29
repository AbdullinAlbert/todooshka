package ru.albertabdullin.todooshka.presentation.dialog.datepicker

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.presentation.dialog.datepicker.model.AvailableDateRange
import java.time.LocalDate
import java.time.ZoneOffset

object DatePickerFactory {
    fun create(
        selectedDate: LocalDate,
        availableDateRange: AvailableDateRange?,
    ): MaterialDatePicker<Long> {
        val validators = buildList {
            if (availableDateRange != null) {
                add(DateValidatorPointForward.from(availableDateRange.startDate.toUtcMillis()))
                add(DateValidatorPointBackward.before(availableDateRange.endDate.toUtcMillis()))
            }
        }

        val constraints = CalendarConstraints.Builder().apply {
            if (availableDateRange != null) {
                setStart(availableDateRange.startDate.toUtcMillis())
                setEnd(availableDateRange.endDate.toUtcMillis())
            }
            if (validators.isNotEmpty()) {
                setValidator(CompositeDateValidator.allOf(validators))
            }
            setOpenAt(selectedDate.toUtcMillis())
        }.build()
        return MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.ThemeOverlay_Todooshka_DatePicker)
            .setSelection(selectedDate.toUtcMillis())
            .setCalendarConstraints(constraints)
            .build()
    }

    private fun LocalDate.toUtcMillis(): Long {
        return atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
}