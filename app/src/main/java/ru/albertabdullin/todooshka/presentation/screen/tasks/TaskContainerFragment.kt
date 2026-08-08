package ru.albertabdullin.todooshka.presentation.screen.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.TaskContainerBinding
import ru.albertabdullin.todooshka.presentation.screen.tasks.daily_representation.DailyRepresentationTasksFragment
import java.time.LocalDate

class TaskContainerFragment : Fragment() {

    private enum class RepresentationTaskTrackerMode {
        Daily, Weekly
    }

    private var representationTaskTrackerMode: RepresentationTaskTrackerMode =
        RepresentationTaskTrackerMode.Daily

    private var selectedDateEpochDay = LocalDate.now().toEpochDay()

    private companion object {
        const val REPRESENTATION_TASK_MODE_KEY = "REPRESENTATION_TASK_MODE"
        const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"
    }

    private var _binding: TaskContainerBinding? = null

    private val actionBar get() = (requireActivity() as AppCompatActivity).supportActionBar

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        savedInstanceState?.also {
            representationTaskTrackerMode =
                RepresentationTaskTrackerMode.valueOf(it.getString(REPRESENTATION_TASK_MODE_KEY)!!)
            selectedDateEpochDay = it.getLong(SELECTED_DATE_KEY)
        }

        _binding = TaskContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTasksRepresentation(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(REPRESENTATION_TASK_MODE_KEY, representationTaskTrackerMode.name)
        outState.putLong(SELECTED_DATE_KEY, selectedDateEpochDay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initTasksRepresentation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                setReorderingAllowed(true)
                add<DailyRepresentationTasksFragment>(
                    R.id.task_container, tag = RepresentationTaskTrackerMode.Daily.name
                )
            }
        }
    }

    private fun initToolbar() {
        requireActivity().addMenuProvider(
            provider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.task_view_representation, menu)
                    val menuItem = menu.findItem(R.id.task_tracker_representation_menu_item)
                    if (representationTaskTrackerMode == RepresentationTaskTrackerMode.Daily) {
                        setupMenuItemForDailyRepresentation(menuItem)
                    } else {
                        setupToolbarForWeeklyRepresentation(menuItem)
                    }

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.task_tracker_select_date_menu_item -> {
                            return true
                        }

                        R.id.task_tracker_representation_menu_item -> {
                            changeTaskTrackerRepresentationMode(menuItem)
                            return true
                        }

                        else -> return false
                    }
                }
            }, owner = viewLifecycleOwner, state = Lifecycle.State.RESUMED
        )
    }

    private fun changeTaskTrackerRepresentationMode(item: MenuItem) {
        val dailyFragment =
            childFragmentManager.findFragmentByTag(RepresentationTaskTrackerMode.Daily.name)
        val weeklyFragment =
            childFragmentManager.findFragmentByTag(RepresentationTaskTrackerMode.Weekly.name)
        when (representationTaskTrackerMode) {
            RepresentationTaskTrackerMode.Daily -> {
                dailyRepresentationMenuItemIsClicked(
                    item = item,
                    dailyFragment = dailyFragment!!,
                    weeklyFragment = weeklyFragment
                )
            }

            RepresentationTaskTrackerMode.Weekly -> {
                weeklyRepresentationMenuItemIsClicked(
                    item = item,
                    dailyFragment = dailyFragment!!,
                    weeklyFragment = weeklyFragment!!
                )
            }
        }
    }

    private fun weeklyRepresentationMenuItemIsClicked(
        item: MenuItem,
        dailyFragment: Fragment,
        weeklyFragment: Fragment
    ) {
        setupMenuItemForDailyRepresentation(item)
        representationTaskTrackerMode = RepresentationTaskTrackerMode.Daily
        childFragmentManager.commitNow {
            setReorderingAllowed(true)

            hide(weeklyFragment)
            setMaxLifecycle(
                weeklyFragment, Lifecycle.State.STARTED
            )

            show(dailyFragment)
            setMaxLifecycle(
                dailyFragment, Lifecycle.State.RESUMED
            )
        }
    }

    private fun setupMenuItemForDailyRepresentation(item: MenuItem) {
        item.title = getString(R.string.weekly_task_representation_mode)
        item.setIcon(R.drawable.date_week_24dp)
        actionBar?.title =
            getString(R.string.daily_task_tracker)
    }

    private fun setupToolbarForWeeklyRepresentation(item: MenuItem) {
        item.title = getString(R.string.daily_task_representation_mode)
        item.setIcon(R.drawable.date_day_24dp)
        actionBar?.title = getString(R.string.weekly_task_tracker)
    }

    private fun dailyRepresentationMenuItemIsClicked(
        item: MenuItem,
        dailyFragment: Fragment,
        weeklyFragment: Fragment?
    ) {
        setupToolbarForWeeklyRepresentation(item)
        representationTaskTrackerMode = RepresentationTaskTrackerMode.Weekly
        childFragmentManager.commitNow {
            setReorderingAllowed(true)

            hide(dailyFragment)
            setMaxLifecycle(
                dailyFragment, Lifecycle.State.STARTED
            )

            if (weeklyFragment == null) {
                add<WeeklyRepresentationTasksFragment>(
                    R.id.task_container, tag = RepresentationTaskTrackerMode.Weekly.name
                )
            } else {
                show(weeklyFragment)
                setMaxLifecycle(
                    weeklyFragment,
                    Lifecycle.State.RESUMED
                )
            }
        }
    }

    fun setSelectedDate(selectedDate: LocalDate) {
        selectedDateEpochDay = selectedDate.toEpochDay()
    }

    fun getSelectedDate(): LocalDate {
        return LocalDate.ofEpochDay(selectedDateEpochDay)
    }
}