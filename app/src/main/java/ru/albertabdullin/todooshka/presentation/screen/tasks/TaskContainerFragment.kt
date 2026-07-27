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
import androidx.lifecycle.Lifecycle
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.databinding.TaskContainerBinding

class TaskContainerFragment : Fragment() {

    private enum class RepresentationTaskTrackerMode {
        Daily, Weekly
    }

    private var representationTaskTrackerMode: RepresentationTaskTrackerMode = RepresentationTaskTrackerMode.Daily

    private companion object {
        const val REPRESENTATION_TASK_MODE_KEY = "REPRESENTATION_TASK_MODE"
    }

    private lateinit var binding: TaskContainerBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        representationTaskTrackerMode = savedInstanceState?.getString(REPRESENTATION_TASK_MODE_KEY)
            ?.let(RepresentationTaskTrackerMode::valueOf)
            ?: RepresentationTaskTrackerMode.Daily
        binding = TaskContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
    }

    private fun initToolbar() {
        requireActivity().addMenuProvider(
            provider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.task_view_representation, menu)
                    val menuItem = menu.findItem(R.id.task_tracker_representation_menu_item)
                    changeTaskTrackerRepresentationMode(menuItem)
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
            },
            owner = viewLifecycleOwner,
            state = Lifecycle.State.RESUMED
        )
    }

    private fun changeTaskTrackerRepresentationMode(item: MenuItem) {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        when (representationTaskTrackerMode) {
            RepresentationTaskTrackerMode.Daily -> {
                item.title = getString(R.string.weekly_representation)
                item.setIcon(R.drawable.date_week_24dp)
                actionBar?.title = getString(R.string.daily_representation)
                representationTaskTrackerMode = RepresentationTaskTrackerMode.Weekly
            }

            RepresentationTaskTrackerMode.Weekly -> {
                item.title = getString(R.string.daily_representation)
                item.setIcon(R.drawable.date_day_24dp)
                actionBar?.title = getString(R.string.weekly_representation)
                representationTaskTrackerMode = RepresentationTaskTrackerMode.Daily
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(REPRESENTATION_TASK_MODE_KEY, REPRESENTATION_TASK_MODE_KEY)
    }
}