package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.DailyRepresentationTaskFragmentBinding
import ru.albertabdullin.todooshka.domain.date_operations.DailyDateRange
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.model.TabPropertyValues
import ru.albertabdullin.todooshka.presentation.screen.tasks.date_tab_scroll.CenteredDateTabSmoothScroller
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

    private var currentSelectedDate: Long = Long.MIN_VALUE

    private val currentSelectedDateKey = "currentSelectedDateKey"

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
        currentSelectedDate =
            savedInstanceState?.getLong(currentSelectedDateKey, Long.MIN_VALUE) ?: Long.MIN_VALUE
        setupTabAdapter()
        subscribeToSelectedDate()
        setupViewPager()
        binding.dailyDateTabList.doOnPreDraw {
            val location = IntArray(2)
            it.getLocationInWindow(location)
            taskContainerViewModel.updateDateTabBottomCoordinate(location[1] + it.height)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(currentSelectedDateKey, currentSelectedDate)
        super.onSaveInstanceState(outState)
    }

    private fun setupViewPager() {
        binding.dailyTaskRepresentationViewPager.adapter =
            DailyTasksPageViewPagerAdapter(this, dailyDateRange)
        binding.dailyTaskRepresentationViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                taskContainerViewModel.onNewDateIsSelected(dailyDateRange.dateAt(position))
            }
        })
    }

    private fun setupTabAdapter() {
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
            }, onDateClick = (taskContainerViewModel::onNewDateIsSelected)
        )
        binding.dailyDateTabList.adapter = tabAdapter
    }

    private fun subscribeToSelectedDate() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.taskContainerState
                    .map { it.dateSelectionChangedArgs }
                    .filter { !it.isDefault }
                    .collect { dateSelectionChangedArs ->
                        if (dateSelectionChangedArs.currentSelectedDayEpoch == currentSelectedDate) return@collect
                        currentSelectedDate = dateSelectionChangedArs.currentSelectedDayEpoch
                        selectNewDateTab(dateSelectionChangedArs)
                        selectNewPage(dateSelectionChangedArs)
                    }
            }
        }
    }

    private fun selectNewPage(dateSelectionChangedArs: DateSelectionChangedArgs) {
        val date = LocalDate.ofEpochDay(dateSelectionChangedArs.currentSelectedDayEpoch)
        binding.dailyTaskRepresentationViewPager.currentItem = dailyDateRange.positionOf(date)
    }

    private fun selectNewDateTab(dateSelectionChangedArs: DateSelectionChangedArgs) {
        if (tabAdapter == null) return
        val layoutManager = binding.dailyDateTabList.layoutManager as? LinearLayoutManager ?: return

        val previousSelectedPos =
            dailyDateRange.positionOf(dateSelectionChangedArs.previousSelectedDayEpoch)
        val currentSelectedPos =
            dailyDateRange.positionOf(dateSelectionChangedArs.currentSelectedDayEpoch)

        val previousSelectedTab = layoutManager.findViewByPosition(previousSelectedPos)
        val currentSelectedTab = layoutManager.findViewByPosition(currentSelectedPos)
        //если предыдущий выбранный таб виден и новый выбранный таб рядом
        val res = isNewSelectedTabNear(
            previousSelectedPos, currentSelectedPos
        )
        if ((previousSelectedTab != null && res) || currentSelectedTab != null) {
            smoothScroll(previousSelectedPos, currentSelectedPos)
        } else {
            instantScroll(
                previousSelectedPos,
                currentSelectedPos,
            )
        }

    }

    private fun isNewSelectedTabNear(previousSelectedPos: Int, currentSelectedPos: Int): Boolean {
        var calculatedWidth = 0
        val helperView = LayoutInflater.from(requireContext())
            .inflate(R.layout.task_tracker_daily_date_tab, binding.dailyDateTabList, false)

        fun helperCalculation(i: Int): Boolean {
            val currentDateText = dailyDateRange.dateAt(i).format(dateTimeFormatter)
            calculatedWidth += getTabWidth(helperView, currentDateText)
            if (calculatedWidth > binding.dailyDateTabList.width) {
                return false
            }
            return true
        }

        if (previousSelectedPos < currentSelectedPos) {
            for (i in (previousSelectedPos + 1)..currentSelectedPos) {
                val calculationResult = helperCalculation(i)
                if (!calculationResult) return false
            }
            return true
        } else {
            for (i in (previousSelectedPos - 1) downTo currentSelectedPos) {
                val calculationResult = helperCalculation(i)
                if (!calculationResult) return false
            }
            return true
        }

    }

    private fun getTabWidth(view: View, text: String): Int {
        view.findViewById<TextView>(R.id.date_tab).text = text
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(
            widthMeasureSpec,
            heightMeasureSpec,
        )
        val marginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        return view.measuredWidth + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin
    }

    private fun smoothScroll(
        previousPos: Int, currentPos: Int
    ) {
        tabAdapter!!.updateSelected(previousPos, currentPos)
        smoothScrollToDateTab(currentPos)
    }

    private fun smoothScrollToDateTab(currentPos: Int) {
        if (tabAdapter == null) return
        val layoutManager = binding.dailyDateTabList.layoutManager as? LinearLayoutManager ?: return
        layoutManager.startSmoothScroll(CenteredDateTabSmoothScroller(requireContext()).apply {
            targetPosition = currentPos
        })
    }


    private fun instantScroll(previousPos: Int, currentPos: Int) {
        val rv = binding.dailyDateTabList
        val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
        rv.visibility = View.INVISIBLE
        rv.postOnAnimation {
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