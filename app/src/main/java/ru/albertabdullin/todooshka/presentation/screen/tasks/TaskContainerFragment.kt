package ru.albertabdullin.todooshka.presentation.screen.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.TaskContainerBinding
import ru.albertabdullin.todooshka.domain.date_operations.LAST_AVAILABLE_DATE
import ru.albertabdullin.todooshka.presentation.dialog.datepicker.DatePickerFactory
import ru.albertabdullin.todooshka.presentation.dialog.datepicker.model.AvailableDateRange
import ru.albertabdullin.todooshka.presentation.extensions.diContainer
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.DailyRepresentationTasksFragment
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.weekly_representation.WeeklyRepresentationTasksFragment
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.RepresentationTaskTrackerMode
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.TaskContainerViewModel
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.TaskTrackerWorkMode
import java.time.Instant
import java.time.ZoneOffset

class TaskContainerFragment : Fragment() {

    private var _binding: TaskContainerBinding? = null

    private val binding get() = _binding!!

    private val taskContainerViewModel: TaskContainerViewModel by viewModels {
        TaskContainerViewModel.factory(
            taskRepository = diContainer().getTaskRepositorySingleton()
        )
    }

    private val editModeMenuProvider = object : MenuProvider {
        override fun onCreateMenu(
            menu: Menu,
            menuInflater: MenuInflater
        ) {
            menuInflater.inflate(R.menu.task_edit_mode, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.undo_action_menu_item -> true
                R.id.redo_action_menu_item -> true
                R.id.save_tasks_menu_item -> true
                else -> false
            }
        }

    }

    private val dailyMenuProviderReadMode = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.task_view_daily_representation_read_mode, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.task_tracker_select_date_menu_item -> {
                    taskContainerViewModel.openCalendarDialogButtonIsClicked()
                    return true
                }

                R.id.task_tracker_weekly_representation_menu_item -> {
                    taskContainerViewModel.onTaskTrackerRepresentationChanged()
                    return true
                }

                else -> return false
            }
        }
    }

    private val weeklyMenuProviderReadMode = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.task_view_weekly_representation_read_mode, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.task_tracker_select_date_menu_item -> {
                    taskContainerViewModel.openCalendarDialogButtonIsClicked()
                    return true
                }

                R.id.task_tracker_daily_representation_menu_item -> {
                    taskContainerViewModel.onTaskTrackerRepresentationChanged()
                    return true
                }

                else -> return false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskContainerViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TaskContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
        initScreen()
        collectOpenCalendarDialogEvents()
    }

    private fun collectOpenCalendarDialogEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.openCalendarEvent
                    .collect { datePickerArgs ->
                        val datePicker = DatePickerFactory.create(
                            selectedDate = datePickerArgs.selectedDate,
                            availableDateRange = AvailableDateRange(
                                startDate = datePickerArgs.firstDate,
                                endDate = LAST_AVAILABLE_DATE
                            )
                        )
                        datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
                            val selectedDate = Instant
                                .ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            taskContainerViewModel.onNewDateIsSelected(selectedDate)
                        }
                        datePicker.show(childFragmentManager, "")
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initTasksRepresentation() {
        childFragmentManager.commitNow {
            setReorderingAllowed(true)
            add<DailyRepresentationTasksFragment>(
                R.id.task_container,
                tag = RepresentationTaskTrackerMode.DAILY.name,
            )
        }
    }

    private fun initToolBar() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskContainerViewModel.taskContainerState
                    .map { it }
                    .collect {
                        when (it.taskTrackerWorkMode) {
                            TaskTrackerWorkMode.READ -> {
                                binding.taskTrackerToolbar.setNavigationIcon(null)
                                binding.taskTrackerToolbar.setNavigationOnClickListener(null)
                                binding.taskTrackerToolbar.removeMenuProvider(editModeMenuProvider)
                                when (it.representationTaskTrackerMode) {
                                    RepresentationTaskTrackerMode.DAILY -> {

                                        binding.taskTrackerToolbar.title =
                                            getString(R.string.daily_task_tracker)
                                        binding.taskTrackerToolbar.removeMenuProvider(
                                            weeklyMenuProviderReadMode
                                        )
                                        binding.taskTrackerToolbar.addMenuProvider(
                                            dailyMenuProviderReadMode
                                        )
                                    }

                                    RepresentationTaskTrackerMode.WEEKLY -> {
                                        binding.taskTrackerToolbar.title =
                                            getString(R.string.weekly_task_tracker)
                                        binding.taskTrackerToolbar.removeMenuProvider(
                                            dailyMenuProviderReadMode
                                        )
                                        binding.taskTrackerToolbar.addMenuProvider(
                                            weeklyMenuProviderReadMode
                                        )
                                    }
                                }
                            }

                            TaskTrackerWorkMode.EDIT -> {
                                binding.taskTrackerToolbar.setNavigationIcon(
                                    ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.arrow_back_24dp,
                                        null
                                    )
                                )
                                binding.taskTrackerToolbar.setNavigationOnClickListener {
                                    taskContainerViewModel.onBackToReadMode()
                                }
                                binding.taskTrackerToolbar.title = ""
                                binding.taskTrackerToolbar.removeMenuProvider(
                                    dailyMenuProviderReadMode
                                )
                                binding.taskTrackerToolbar.removeMenuProvider(
                                    weeklyMenuProviderReadMode
                                )
                                binding.taskTrackerToolbar.addMenuProvider(editModeMenuProvider)
                            }

                            TaskTrackerWorkMode.SEARCH -> {

                            }
                        }
                    }
            }
        }
    }

    private fun initScreen() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskContainerViewModel.taskContainerState
                    .map { it.representationTaskTrackerMode }
                    .distinctUntilChanged()
                    .collect {
                        val dailyFragment =
                            childFragmentManager.findFragmentByTag(
                                RepresentationTaskTrackerMode.DAILY.name
                            )
                        val weeklyFragment =
                            childFragmentManager.findFragmentByTag(
                                RepresentationTaskTrackerMode.WEEKLY.name
                            )

                        when (it) {
                            RepresentationTaskTrackerMode.DAILY -> {
                                if (dailyFragment == null) {
                                    initTasksRepresentation()
                                } else {
                                    setupTaskTrackerDailyRepresentation(
                                        dailyFragment = dailyFragment,
                                        weeklyFragment = weeklyFragment!!
                                    )
                                }
                            }

                            RepresentationTaskTrackerMode.WEEKLY -> {
                                setupTaskTrackerWeeklyRepresentation(
                                    dailyFragment = dailyFragment!!,
                                    weeklyFragment = weeklyFragment
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun setupTaskTrackerDailyRepresentation(
        dailyFragment: Fragment,
        weeklyFragment: Fragment
    ) {
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

    private fun setupTaskTrackerWeeklyRepresentation(
        dailyFragment: Fragment,
        weeklyFragment: Fragment?
    ) {
        childFragmentManager.commitNow {
            setReorderingAllowed(true)

            hide(dailyFragment)
            setMaxLifecycle(dailyFragment, Lifecycle.State.STARTED)

            if (weeklyFragment == null) {
                add<WeeklyRepresentationTasksFragment>(
                    R.id.task_container,
                    RepresentationTaskTrackerMode.WEEKLY.name
                )
            } else {
                show(weeklyFragment)
                setMaxLifecycle(weeklyFragment, Lifecycle.State.RESUMED)
            }
        }
    }

}