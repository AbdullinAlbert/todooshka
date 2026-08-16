package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.screen.tasks.date_tab_scroll.CenteredDateTabSmoothScroller
import ru.albertabdullin.todooshka.presentation.screen.tasks.model.TabPropertyValues
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters.RecyclerViewDailyAdapter
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
        ownerProducer = { requireParentFragment() })

    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru-RU"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DailyRepresentationTaskFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyDateRange = DailyDateRange(firstDate = LocalDate.now(), lastDate = LAST_AVAILABLE_DATE)
        tabAdapter = RecyclerViewDailyAdapter(
            dailyDateRange = dailyDateRange, tabPropertyValuesProvider = { date ->
                if (date.dayOfMonth > 26) {
                    Log.d(DailyRepresentationTasksFragment::class.simpleName, "localDate = $date")
                }
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
            }, onDateClick = (taskContainerViewModel::onNewDateIsSelected)
        )
        binding.dailyDateTab.adapter = tabAdapter
        collectScrollToDateEvents()
    }

    private fun collectScrollToDateEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.scrollDateTabEvent.collect { selectedDate ->
                    if (!this@DailyRepresentationTasksFragment::dailyDateRange.isInitialized) return@collect
                    val targetPos = dailyDateRange.positionOf(selectedDate)
                    val layoutManager =
                        binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return@collect
                    val targetView = layoutManager.findViewByPosition(targetPos)
                    if (targetView == null) {
                        testInstantScroll(targetPos - 1, targetPos)
                    } else {
                        smoothScrollToDateTab(targetPos)
                    }
                }
            }
        }
    }

    private fun smoothScrollToDateTab(position: Int) {
        val layoutManager = binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return
        layoutManager.startSmoothScroll(CenteredDateTabSmoothScroller(requireContext()).apply {
            targetPosition = position
        })
    }


    private fun testInstantScroll(aroundPos: Int, targetPos: Int) {
        val rv = binding.dailyDateTab
        val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
        rv.visibility = View.INVISIBLE
        rv.post {
            rv.doOnNextLayout {
                rv.visibility = View.VISIBLE
                smoothScrollToDateTab(targetPos)
            }
            layoutManager.scrollToPosition(aroundPos)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tabAdapter = null
    }

}