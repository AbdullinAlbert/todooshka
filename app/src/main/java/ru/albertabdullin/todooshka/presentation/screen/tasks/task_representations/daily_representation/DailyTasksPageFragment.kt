package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.albertabdullin.todooshka.databinding.DailyRepresentationPageFragmentBinding
import ru.albertabdullin.todooshka.presentation.extensions.diContainer
import ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters.TasksAdapter
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.BackButtonIsClicked
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.DailyTasksPageViewModel
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.SaveButtonIsClicked
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.TaskContainerViewModel
import java.time.LocalDate

class DailyTasksPageFragment : Fragment() {

    private var _binding: DailyRepresentationPageFragmentBinding? = null

    private val binding get() = _binding!!

    private var tasksAdapter: TasksAdapter? = null

    private val dailyTasksPageViewModel: DailyTasksPageViewModel by viewModels {
        DailyTasksPageViewModel.factory(
            tasksUseCase = diContainer().getTasksUseCaseInstance(),
            dateForPage = LocalDate.ofEpochDay(requireArguments().getLong(DATE_KEY))
        )
    }

    private val taskContainerViewModel: TaskContainerViewModel by viewModels(
        ownerProducer = { requireParentFragment().requireParentFragment() }
    )

    private var tasksListTopPadding: Int = 0

    companion object {

        fun getInstance(date: LocalDate): DailyTasksPageFragment {
            val args = Bundle().apply {
                putLong(DATE_KEY, date.toEpochDay())
            }
            return DailyTasksPageFragment().apply {
                arguments = args
            }
        }

        private const val TASKS_LIST_TOP_PADDING_KEY: String = "TASKS_LIST_TOP_PADDING_KEY"

        private const val DATE_KEY: String = "DATE_KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DailyRepresentationPageFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tasksListTopPadding = savedInstanceState?.getInt(TASKS_LIST_TOP_PADDING_KEY) ?: 0
        setupTasksList()
        subscribeToState()
        subscribeToSaveTasksResult()
        subscribeToTDateTabPadding()
        subscribeToAppBarItemMenuClicks()
    }

    private fun setupTasksList() {
        tasksAdapter = TasksAdapter(
            onSettingsClick = { task -> },
            onFocusAtTaskDescription = taskContainerViewModel::onTaskDescriptionFocus,
            onSubmitTask = dailyTasksPageViewModel::onTaskSubmitted,
            onTaskDescriptionChanged = dailyTasksPageViewModel::onTaskDescriptionChanged,
            onDeleteTask = dailyTasksPageViewModel::onTaskDeleted,
            isActiveTask = dailyTasksPageViewModel::isActiveTask
        )
        binding.tasksList.adapter = tasksAdapter
        binding.tasksList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun subscribeToState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dailyTasksPageViewModel.taskList.collect {
                    val adapter = tasksAdapter ?: return@collect
                    adapter.submitList(it) {
                        val binding = _binding ?: return@submitList
                        val layoutManager = binding.tasksList.layoutManager as? LinearLayoutManager
                            ?: return@submitList
                        val pos = dailyTasksPageViewModel.getActiveTaskPosition()
                        if (pos < 0) return@submitList
                        val view = layoutManager.findViewByPosition(pos)
                        if (view == null) {
                            layoutManager.scrollToPosition(pos)
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToTDateTabPadding() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskContainerViewModel.dateTabBottomCoordinate.filter { it > 0 }
                    .collect { dateTabBottomCoordinate ->
                        if (tasksListTopPadding == 0) {
                            val location = IntArray(2)
                            binding.tasksList.getLocationInWindow(location)
                            tasksListTopPadding = (dateTabBottomCoordinate - location[1])
                        }
                        binding.tasksList.updatePadding(top = tasksListTopPadding)
                        binding.tasksList.doOnNextLayout { tasksList ->
                            tasksList.visibility = View.VISIBLE
                        }
                    }
            }
        }
    }

    private fun subscribeToAppBarItemMenuClicks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                taskContainerViewModel.appBarItemMenuEvent
                    .collect {
                        when (it) {
                            BackButtonIsClicked -> TODO()
                            SaveButtonIsClicked -> dailyTasksPageViewModel.saveTasksButtonIsClicked()
                        }
                    }
            }
        }
    }

    private fun subscribeToSaveTasksResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                dailyTasksPageViewModel.saveTasksResultEvents
                    .collect {
                        if (it is DailyTasksPageViewModel.SaveTasksResultStub) return@collect
                        taskContainerViewModel.onTasksSaved()
                        dailyTasksPageViewModel.saveTaskResultEventIsHandled()
                    }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(TASKS_LIST_TOP_PADDING_KEY, tasksListTopPadding)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksAdapter = null
        _binding = null
    }
}