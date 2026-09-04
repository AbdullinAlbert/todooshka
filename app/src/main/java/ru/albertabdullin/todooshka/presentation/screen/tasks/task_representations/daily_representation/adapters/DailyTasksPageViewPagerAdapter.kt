package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.DailyTasksPageFragment

class DailyTasksPageViewPagerAdapter(
    fragment: Fragment,
    private val dailyDateRange: DailyDateRange
) :
    FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return DailyTasksPageFragment.getInstance(dailyDateRange.dateAt(position))
    }

    override fun getItemCount(): Int {
        return dailyDateRange.size
    }
}