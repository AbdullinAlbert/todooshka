package ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.albertabdullin.todooshka.databinding.TaskTrackerDailyDateTabBinding
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class RecyclerViewDailyAdapter(
    private var firstAvailableDate: LocalDate,
    private val onDateClick: (LocalDate) -> Unit,
) : RecyclerView.Adapter<DailyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TaskTrackerDailyDateTabBinding.inflate(inflater)
        return DailyViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return ChronoUnit.DAYS.between(firstAvailableDate, LAST_AVAILABLE_DATE).toInt() + 1
    }

    fun updateFirstAvailableDate(newDate: LocalDate) {
        if (newDate == firstAvailableDate) return

        val previousDate = firstAvailableDate

        if (newDate.isBefore(previousDate)) {
            val insertedCount = ChronoUnit.DAYS.between(newDate, previousDate).toInt()

            firstAvailableDate = newDate
            notifyItemRangeInserted(0, insertedCount)
        }
    }

    companion object {
        private val LAST_AVAILABLE_DATE = LocalDate.of(2127, 1, 1)
    }
}

class DailyViewHolder(binding: TaskTrackerDailyDateTabBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind() {

    }
}