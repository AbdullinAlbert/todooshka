package ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation.adapters.RecyclerViewDailyAdapter
import ru.albertabdullin.todooshka.presentation.screen.tasks.model.TabPropertyValues
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.TaskContainerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyRepresentationTasksFragment : Fragment() {
    private var _binding: DailyRepresentationTaskFragmentBinding? = null
    private val binding get() = _binding!!
    private var tabAdapter: RecyclerViewDailyAdapter? = null
    private lateinit var dailyDateRange: DailyDateRange

    private val taskContainerViewModel: TaskContainerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru-RU"))

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
                    taskContainerViewModel.isSelectedDate(date) -> {
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
                    formattedText = date.format(dateTimeFormatter),
                    background = ResourcesCompat.getDrawable(resources, background, null)!!,
                    textColor = textColor
                )
            },
            onDateClick = (taskContainerViewModel::setSelectedDate)
        )
        binding.dailyDateTab.adapter = tabAdapter
        paintSelectedDateTab()
        collectScrollToDateEvents()
    }

    private fun paintSelectedDateTab() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.selectedDate.collect { selectedDate ->
                    tabAdapter?.setSelectedDate(selectedDate)
                }
            }
        }
    }

    private fun collectScrollToDateEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.scrollDateTabEvent.collect { selectedDate ->

                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tabAdapter = null
    }

}