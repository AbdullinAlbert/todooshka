package ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.screen.tasks.TaskContainerFragment
import ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation.adapters.RecyclerViewDailyAdapter
import ru.albertabdullin.todooshka.presentation.screen.tasks.model.TabPropertyValues
import java.time.LocalDate

class DailyRepresentationTasksFragment : Fragment() {
    private var _binding: DailyRepresentationTaskFragmentBinding? = null
    private val binding get() = _binding!!
    private var tabAdapter: RecyclerViewDailyAdapter? = null
    private lateinit var dailyDateRange: DailyDateRange

    private val taskContainerFragment get() = requireParentFragment() as TaskContainerFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DailyRepresentationTaskFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyDateRange = DailyDateRange(firstDate = LocalDate.now(), lastDate = LAST_AVAILABLE_DATE)
        tabAdapter = RecyclerViewDailyAdapter(
            dailyDateRange = dailyDateRange,
            tabPropertyValuesProvider = { date ->
                var background: Int
                var textColor: Int
                when {
                    date.isEqual(getSelectedDate()) -> {
                        background = R.drawable.selected_task_tracker_date_tab_background
                        textColor = R.color.task_tracker_selected_date_tab_text_color
                    }

                    date.isEqual(LocalDate.now()) -> {
                        background = R.drawable.task_tracker_today_date_tab_background
                        textColor = R.color.task_tracker_today_date_tab_text_color
                    }

                    else -> {
                        background = R.drawable.task_tracker_date_tab_background
                        textColor = R.color.black
                    }
                }
                TabPropertyValues(
                    formattedText = "14.04.1994 (пн)",
                    background = ResourcesCompat.getDrawable(resources, background, null)!!,
                    textColor = textColor
                )
            },
            onDateClick = { currentSelectedDate ->
                val previousSelectedDate = getSelectedDate()
                setSelectedDate(currentSelectedDate)
                val previousSelectedTabPosition = dailyDateRange.positionOf(previousSelectedDate)
                val currentSelectedTabPosition = dailyDateRange.positionOf(currentSelectedDate)
                tabAdapter?.apply {
                    notifyItemChanged(previousSelectedTabPosition)
                    notifyItemChanged(currentSelectedTabPosition)
                }
            }
        )
        binding.dailyDateTab.apply {
            adapter = tabAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tabAdapter = null
    }

    private fun getSelectedDate(): LocalDate {
        return taskContainerFragment.getSelectedDate()
    }

    private fun setSelectedDate(selectedDate: LocalDate) {
        taskContainerFragment.setSelectedDate(selectedDate)
    }
}