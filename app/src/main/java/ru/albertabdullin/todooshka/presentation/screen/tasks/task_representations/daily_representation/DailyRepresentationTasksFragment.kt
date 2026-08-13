package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.screen.tasks.calculation.DateTabScrollCalculator
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

    private val dateTabScrollCalculator = DateTabScrollCalculator()

    private val taskContainerViewModel: TaskContainerViewModel by viewModels(
        ownerProducer = { requireParentFragment() })

    private val tabSmoothScroller by lazy { CenteredDateTabSmoothScroller(requireContext()) }

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
            }, onDateClick = (taskContainerViewModel::setSelectedDate)
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskContainerViewModel.scrollDateTabEvent.collect { selectedDate ->
                    if (!this@DailyRepresentationTasksFragment::dailyDateRange.isInitialized) return@collect
                    val targetPos = dailyDateRange.positionOf(selectedDate)
                    val layoutManager =
                        binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return@collect
                    val targetView = layoutManager.findViewByPosition(targetPos)
                    if (targetView == null) {
                        scrollInstantlyToDateTab(targetPos)
                        return@collect
                    }
                    val scrollResult = dateTabScrollCalculator.calculate(
                        rvLeft = binding.dailyDateTab.paddingLeft,
                        rvRight = binding.dailyDateTab.width - binding.dailyDateTab.paddingRight,
                        itemLeft = layoutManager.getDecoratedLeft(targetView),
                        itemRight = layoutManager.getDecoratedRight(targetView)
                    )
                    when (scrollResult) {
                        DateTabScrollCalculator.ScrollResult.SMOOTH_SCROLL -> smoothScrollToDateTab(
                            targetPos
                        )

                        DateTabScrollCalculator.ScrollResult.INSTANT_SCROLL -> scrollInstantlyToDateTab(
                            targetPos
                        )
                    }
                }
            }
        }
    }

    private fun smoothScrollToDateTab(position: Int) {
        val layoutManager = binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return
        tabSmoothScroller.targetPosition = position
        layoutManager.startSmoothScroll(tabSmoothScroller)
    }

    private fun scrollInstantlyToDateTab(position: Int) {
        val recyclerView = binding.dailyDateTab
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val itemWidth = recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.width

        if (itemWidth != null) {
            val offset = (recyclerView.width - itemWidth) / 2
            layoutManager.scrollToPositionWithOffset(position, offset)
        } else {
            layoutManager.scrollToPosition(position)

            recyclerView.post {
                val targetView = layoutManager.findViewByPosition(position) ?: return@post

                val offset = (recyclerView.width - targetView.width) / 2
                layoutManager.scrollToPositionWithOffset(position, offset)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tabAdapter = null
    }

}