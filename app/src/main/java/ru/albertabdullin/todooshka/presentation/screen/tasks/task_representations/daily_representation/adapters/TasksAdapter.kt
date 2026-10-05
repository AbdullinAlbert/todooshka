package ru.albertabdullin.todooshka.presentation.screen.tasks.task_representations.daily_representation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.albertabdullin.todooshka.databinding.SimpleTaskFormItemBinding
import ru.albertabdullin.todooshka.presentation.model.TaskUi


class TasksAdapter(
    private val onSettingsClick: (TaskUi) -> Unit,
    private val onSubmitTask: (Int, String, String) -> Unit,
    private val onDeleteTask: (Int, String) -> Unit,
    private val onTaskDescriptionChanged: (Int, String) -> Unit,
    private val isActiveTask: (TaskUi) -> Boolean
) : ListAdapter<TaskUi, TasksAdapter.TaskViewHolder>(TaskDiffCallback) {

    private val simpleTaskViewType = 0
    private val groupTaskViewType = 1

    private data object TextChangedPayload

    override fun getItemViewType(position: Int): Int {
        val task = getItem(position)
        return if (task.isGroupTask) groupTaskViewType else simpleTaskViewType
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

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int, payloads: List<Any?>) {
        if (TextChangedPayload in payloads) return
        super.onBindViewHolder(holder, position, payloads)
    }

    inner class SimpleTaskViewHolder(private val binding: SimpleTaskFormItemBinding) :
        TaskViewHolder(binding.root) {
        override fun bind(task: TaskUi) {
            binding.simpleTaskForm.init(task)
            binding.simpleTaskForm.setOnSettingsButtonClickListener {
                onSettingsClick(task)
            }
            binding.simpleTaskForm.onSubmitTask =
                { submittedTaskDescriptionPart1, submittedTaskDescriptionPart2 ->
                    onSubmitTask(
                        task.id, submittedTaskDescriptionPart1, submittedTaskDescriptionPart2
                    )
                }
            binding.simpleTaskForm.onDeleteTaskClick = { onDeleteTask(task.id, it) }
            binding.simpleTaskForm.onTextChanged =
                { text -> onTaskDescriptionChanged(task.id, text) }
            if (isActiveTask(task)) {
                binding.simpleTaskForm.requestFocusOnTask(task.selectionPosition)
            } else {
                binding.simpleTaskForm.clearFocusOnTask()
            }
        }
    }

    open class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open fun bind(task: TaskUi) {}
    }

    private object TaskDiffCallback : DiffUtil.ItemCallback<TaskUi>() {
        override fun areItemsTheSame(
            oldItem: TaskUi, newItem: TaskUi
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskUi, newItem: TaskUi
        ): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: TaskUi, newItem: TaskUi): Any? {
            return if (
                (oldItem != newItem) &&
                (oldItem.copy(description = newItem.description) == newItem)
            ) TextChangedPayload else null
        }
    }
}