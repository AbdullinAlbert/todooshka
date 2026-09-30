package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
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
import ru.albertabdullin.todooshka.presentation.screen.tasks.viewmodel.DailyTasksPageViewModel
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
        ownerProducer = { requireParentFragment().requireParentFragment() })


    companion object {

        fun getInstance(date: LocalDate): DailyTasksPageFragment {
            val args = Bundle().apply {
                putLong(DATE_KEY, date.toEpochDay())
            }
            return DailyTasksPageFragment().apply {
                arguments = args
            }
        }

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
        setupTasksList()
        subscribeToState()
        subscribeToTDateTabPadding()
    }

    private fun setupTasksList() {
        tasksAdapter = TasksAdapter(
            onSettingsClick = { task -> },
            onSubmitTask = { submittedTaskPosition, taskDescription ->
                dailyTasksPageViewModel.onTaskSubmitted(
                    submittedTaskPosition,
                    taskDescription
                )
            },
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
                    if (tasksAdapter == null) return@collect
                    tasksAdapter!!.submitList(it) {
                        val layoutManager = binding.tasksList.layoutManager as? LinearLayoutManager
                            ?: return@submitList
                        val pos = dailyTasksPageViewModel.getActiveTaskPosition()
                        if (pos < 0) return@submitList
                        val view = layoutManager.findViewByPosition(pos)
                        if (view == null) {
                            layoutManager.scrollToPosition(it.size - 1)
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
                        binding.tasksList.doOnPreDraw { tasksList ->
                            val location = IntArray(2)
                            tasksList.getLocationInWindow(location)
                            val tasksListTopPadding =
                                (dateTabBottomCoordinate - location[1])
                            tasksList.updatePadding(top = tasksListTopPadding)
                            tasksList.visibility = View.VISIBLE
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksAdapter = null
        _binding = null
    }
}