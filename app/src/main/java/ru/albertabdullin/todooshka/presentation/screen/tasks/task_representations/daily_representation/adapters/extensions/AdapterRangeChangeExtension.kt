package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters.extensions

import androidx.recyclerview.widget.RecyclerView
import ru.albertabdullin.todooshka.domain.date_operations.DateRangeChange

fun RecyclerView.Adapter<*>.updateListRange(dateRangeChange: DateRangeChange) {
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
