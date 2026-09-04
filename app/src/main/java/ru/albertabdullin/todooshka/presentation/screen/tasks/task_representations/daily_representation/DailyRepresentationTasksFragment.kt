package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
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
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.screen.tasks.date_tab_scroll.CenteredDateTabSmoothScroller
import ru.albertabdullin.todooshka.presentation.screen.tasks.model.TabPropertyValues
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters.DailyTasksPageViewPagerAdapter
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters.RecyclerViewDailyAdapter
import ru.albertabdullin.todooshka.presentation.screen.tasks.value_object.DateSelectionChangedArgs
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.TaskContainerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyRepresentationTasksFragment : Fragment() {
    private var _binding: DailyRepresentationTaskFragmentBinding? = null
    private val binding get() = _binding!!
    private var tabAdapter: RecyclerViewDailyAdapter? = null
    private val dailyDateRange =
        DailyDateRange(firstDate = LocalDate.now(), lastDate = LAST_AVAILABLE_DATE)

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
        setupTabAdapter()
        collectScrollToDateEvents()
        setupViewPager()
    }

    private fun setupViewPager() {
        binding.dailyTaskRepresentationViewPager.adapter =
            DailyTasksPageViewPagerAdapter(this, dailyDateRange)
        binding.dailyTaskRepresentationViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                taskContainerViewModel.onNewDateIsSelectedFromCalendar(
                    dailyDateRange.dateAt(
                        position
                    )
                )
            }
        })
    }

    private fun setupTabAdapter() {
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
            }, onDateClick = (taskContainerViewModel::onNewDateIsSelectedFromTabs)
        )
        binding.dailyDateTab.adapter = tabAdapter
    }

    private fun collectScrollToDateEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.scrollDateEvent.collect { dateSelectionChangedArs ->
                    scrollEventForTabs(dateSelectionChangedArs)
                    scrollEventForPages(dateSelectionChangedArs)
                }
            }
        }
    }

    private fun scrollEventForPages(dateSelectionChangedArs: DateSelectionChangedArgs) {
        val date = LocalDate.ofEpochDay(dateSelectionChangedArs.currentSelectedDayEpoch)
        binding.dailyTaskRepresentationViewPager.currentItem = dailyDateRange.positionOf(date)
    }

    private fun scrollEventForTabs(dateSelectionChangedArs: DateSelectionChangedArgs) {
        if (tabAdapter == null) return
        val layoutManager =
            binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return
        val previousSelectedPos =
            dailyDateRange.positionOf(LocalDate.ofEpochDay(dateSelectionChangedArs.previousSelectedDayEpoch))
        val currentSelectedPos =
            dailyDateRange.positionOf(LocalDate.ofEpochDay(dateSelectionChangedArs.currentSelectedDayEpoch))
        val targetView = layoutManager.findViewByPosition(currentSelectedPos)
        if (targetView == null) {
            instantScroll(previousSelectedPos, currentSelectedPos)
        } else {
            tabAdapter!!.updateSelected(previousSelectedPos, currentSelectedPos)
            smoothScrollToDateTab(currentSelectedPos)
        }
    }

    private fun smoothScrollToDateTab(currentPos: Int) {
        if (tabAdapter == null) return
        val layoutManager = binding.dailyDateTab.layoutManager as? LinearLayoutManager ?: return
        layoutManager.startSmoothScroll(CenteredDateTabSmoothScroller(requireContext()).apply {
            targetPosition = currentPos
        })
    }


    private fun instantScroll(previousPos: Int, currentPos: Int) {
        val rv = binding.dailyDateTab
        val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
        rv.visibility = View.INVISIBLE
        rv.post {
            rv.doOnNextLayout {
                rv.visibility = View.VISIBLE
                smoothScrollToDateTab(currentPos)
            }
            val aroundPos = if (previousPos < currentPos) currentPos - 1 else currentPos + 1
            layoutManager.scrollToPosition(aroundPos)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tabAdapter = null
    }

}