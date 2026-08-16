package ru.albertabdullin.todooshka.presentation.dialog.datepicker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import ru.albertabdullin.todooshka.presentation.dialog.datepicker.model.AvailableDateRange
import java.time.LocalDate
import java.time.ZoneId

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    companion object {

        fun newInstance(
            availableDateRange: AvailableDateRange?,
            selectedDateEpochDay: Long
        ): DatePickerFragment {
            val bundle = Bundle()
            if (availableDateRange != null) {
                bundle.putLong(START_DATE, availableDateRange.startEpochDay)
                bundle.putLong(END_DATE, availableDateRange.endEpochDay)
            }
            bundle.putLong(SELECTED_DATE_ARG_KEY, selectedDateEpochDay)
            val fragment = DatePickerFragment()
            fragment.arguments = bundle
            return fragment
        }

        private const val START_DATE = "START_DATE"
        private const val END_DATE = "END_DATE"
        const val SELECTED_DATE_ARG_KEY = "SELECTED_DATE"

        const val SELECTED_DATE_REQUEST_KEY = "SELECTED_DATE_REQUEST_KEY"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()

        require(args.containsKey(SELECTED_DATE_ARG_KEY)) {
            "SELECTED_DATE argument is required"
        }

        val selectedDate = LocalDate.ofEpochDay(
            args.getLong(SELECTED_DATE_ARG_KEY)
        )

        val dialog = DatePickerDialog(
            requireContext(),
            this,
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )

        if (args.containsKey(START_DATE)) {
            val startDate = LocalDate.ofEpochDay(
                args.getLong(START_DATE)
            )

            dialog.datePicker.minDate = startDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        if (args.containsKey(END_DATE)) {
            val endDate = LocalDate.ofEpochDay(
                args.getLong(END_DATE)
            )

            dialog.datePicker.maxDate = endDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        return dialog
    }

    override fun onDateSet(
        view: DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        val bundle = Bundle()
        bundle.putLong(SELECTED_DATE_ARG_KEY, LocalDate.of(year, month + 1, dayOfMonth).toEpochDay())
        parentFragmentManager.setFragmentResult(SELECTED_DATE_REQUEST_KEY, bundle)
    }
}