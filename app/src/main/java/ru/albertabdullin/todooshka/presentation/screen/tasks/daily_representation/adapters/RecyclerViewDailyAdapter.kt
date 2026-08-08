package ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.albertabdullin.todooshka.databinding.TaskTrackerDailyDateTabBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.DateRangeChange
import ru.albertabdullin.todooshka.presentation.screen.tasks.model.TabPropertyValues
import java.time.LocalDate

class RecyclerViewDailyAdapter(
    private val dailyDateRange: DailyDateRange,
    private val tabPropertyValuesProvider: (LocalDate) -> TabPropertyValues,
    private val onDateClick: (LocalDate) -> Unit,
) : RecyclerView.Adapter<RecyclerViewDailyAdapter.DailyViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TaskTrackerDailyDateTabBinding.inflate(inflater)
        return DailyViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(dailyDateRange.dateAt(position))
    }

    override fun getItemId(position: Int): Long {
        return dailyDateRange.dateAt(position).toEpochDay()
    }

    override fun getItemCount() = dailyDateRange.size

    fun updateListRange(dateRangeChange: DateRangeChange) {
        when (dateRangeChange) {
            is DateRangeChange.None -> return
            is DateRangeChange.InsertedAtStart -> {
                notifyItemRangeInserted(0, dateRangeChange.count)
            }

            is DateRangeChange.RemovedFromStart -> {
                notifyItemRangeRemoved(0, dateRangeChange.count)
            }
        }
    }

    inner class DailyViewHolder(private val binding: TaskTrackerDailyDateTabBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: LocalDate) {
            binding.dateTab.setOnClickListener { onDateClick(date) }
            val tabPropertyValues = tabPropertyValuesProvider(date)
            binding.dateTab.text = tabPropertyValues.formattedText
            binding.dateTab.background = tabPropertyValues.background
            binding.dateTab.setTextColor(tabPropertyValues.textColor)
        }
    }

}