package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.albertabdullin.todooshka.databinding.SimpleTaskFormItemBinding
import ru.albertabdullin.todooshka.domain.entity.ComplexTask
import ru.albertabdullin.todooshka.domain.entity.NewTask
import ru.albertabdullin.todooshka.domain.entity.SimpleTask
import ru.albertabdullin.todooshka.domain.entity.Task

class TasksAdapter(
    private val onSettingsClick: (Task) -> Unit,
    private val onSubmitTask: (Int, String, String) -> Unit,
    private val isActiveTask: (Task) -> Boolean
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback) {

    private val simpleTaskViewType = 0
    private val complexTaskViewType = 1

    override fun getItemViewType(position: Int): Int {
        val task = getItem(position)
        return when (task) {
            is NewTask, is SimpleTask -> simpleTaskViewType
            is ComplexTask -> complexTaskViewType
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            simpleTaskViewType -> {
                val binding = SimpleTaskFormItemBinding.inflate(inflater, parent, false)
                SimpleTaskViewHolder(binding)
            }

            else -> error("Неизвестный viewType: $viewType")

        }

    }

    override fun onBindViewHolder(
        holder: TaskViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class SimpleTaskViewHolder(private val binding: SimpleTaskFormItemBinding) :
        TaskViewHolder(binding.root) {
        override fun bind(task: Task) {
            binding.simpleTaskForm.setTask(task)
            binding.simpleTaskForm.setOnSettingsButtonClickListener {
                onSettingsClick(task)
            }
            binding.simpleTaskForm.onSubmitTask =
                { submittedTaskDescriptionPart1, submittedTaskDescriptionPart2 ->
                    onSubmitTask(
                        task.id,
                        submittedTaskDescriptionPart1,
                        submittedTaskDescriptionPart2
                    )
                }
            if (isActiveTask(task)) {
                binding.simpleTaskForm.requestFocusOnTask()
            } else {
                binding.simpleTaskForm.clearFocusOnTask()
            }
        }
    }

    open class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open fun bind(task: Task) {}
    }

    private object TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(
            oldItem: Task, newItem: Task
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Task, newItem: Task
        ): Boolean {
            return oldItem == newItem
        }
    }
}