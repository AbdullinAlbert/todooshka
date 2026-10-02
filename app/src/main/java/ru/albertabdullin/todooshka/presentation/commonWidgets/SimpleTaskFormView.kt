package ru.albertabdullin.todooshka.presentation.commonWidgets

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.checkbox.MaterialCheckBox
import ru.albertabdullin.todooshka.R
import ru.albertabdullin.todooshka.domain.entity.Task

class SimpleTaskFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onSubmitTask: ((String, String) -> Unit)? = null

    private var _taskDescriptionEditText: TaskEditText? = null
    private val taskDescriptionEditText: TaskEditText
        get() = _taskDescriptionEditText!!

    private var _settingsButton: AppCompatImageView? = null
    private val settingsButton: AppCompatImageView
        get() = _settingsButton!!

    private var _checkbox: MaterialCheckBox? = null

    private val checkbox: MaterialCheckBox
        get() = _checkbox!!


    init {
        LayoutInflater.from(context).inflate(R.layout.simple_task_form, this, true)

        _taskDescriptionEditText = findViewById(R.id.simple_task_description_form)
        _settingsButton = findViewById(R.id.simple_task_settings)
        _checkbox = findViewById(R.id.simple_task_checkBox)

        settingsButton.isEnabled = false
        checkbox.isEnabled = false

        taskDescriptionEditText.onSubmitTask = submit@{
            val taskDescriptionText = taskDescriptionEditText.text?.toString() ?: ""
            val selectionStart = taskDescriptionEditText.selectionStart
            val selectionEnd = taskDescriptionEditText.selectionEnd
            if (selectionStart == -1 || selectionEnd == -1) {
                return@submit
            }

            val part1 = taskDescriptionText.substring(0, selectionStart)
            val part2 =
                taskDescriptionText.substring(selectionEnd.coerceAtMost(taskDescriptionText.length))
            onSubmitTask?.invoke(part1, part2)
        }

        taskDescriptionEditText.doAfterTextChanged { _ ->
            updateTaskFormState()
        }

        checkbox.setOnCheckedChangeListener { _, _ ->
            updateTaskFormState()
        }
    }

    private fun updateTaskFormState() {
        val taskDescriptionIsNotBlank =
            (taskDescriptionEditText.text?.toString() ?: "").isNotBlank()
        val taskIsChecked = checkbox.isChecked

        settingsButton.isEnabled = !taskIsChecked && taskDescriptionIsNotBlank
        taskDescriptionEditText.isEnabled = !taskIsChecked
        checkbox.isEnabled = taskDescriptionIsNotBlank

        if (taskIsChecked) {
            taskDescriptionEditText.paintFlags =
                taskDescriptionEditText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            taskDescriptionEditText.paintFlags =
                taskDescriptionEditText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun setTask(task: Task) {
        checkbox.isChecked = task.isCompleted
        if (taskDescriptionEditText.text?.toString() != task.description) {
            taskDescriptionEditText.setText(task.description)
            taskDescriptionEditText.resetIsDeletingHandled()
        }
    }

    fun requestFocusOnTask() {
        taskDescriptionEditText.requestFocus()
        taskDescriptionEditText.setSelection(0)
    }

    fun clearFocusOnTask() {
        taskDescriptionEditText.clearFocus()
    }

    fun setOnSettingsButtonClickListener(clickListener: () -> Unit) {
        val defaultIconColor = ContextCompat.getColor(context, R.color.black)
        val clickedIconColor =
            ContextCompat.getColor(context, R.color.task_settings_icon_button_clicked)

        val defaultButtonBackground =
            ContextCompat.getColor(context, R.color.white)
        val clickedButtonBackground =
            ContextCompat.getColor(context, R.color.task_settings_background_button_pressed)

        val iconColorAnimator = ValueAnimator.ofArgb(defaultIconColor, clickedIconColor).apply {
            duration = 400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
            addUpdateListener { animator ->
                settingsButton.imageTintList = ColorStateList.valueOf(animator.animatedValue as Int)
            }
        }
        iconColorAnimator.doOnEnd {
            settingsButton.imageTintList =
                ContextCompat.getColorStateList(
                    context,
                    R.color.task_settings_icon_states
                )
        }

        val backgroundButtonAnimator = ValueAnimator.ofArgb(
            defaultButtonBackground,
            clickedButtonBackground
        ).apply {
            duration = 400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
            addUpdateListener { animator ->
                settingsButton.backgroundTintList =
                    ColorStateList.valueOf(animator.animatedValue as Int)
            }
        }

        settingsButton.setOnClickListener { view ->
            backgroundButtonAnimator.cancel()
            iconColorAnimator.cancel()
            view.animate().cancel()

            backgroundButtonAnimator.start()
            iconColorAnimator.start()

            view.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(200)
                .withEndAction {
                    view.animate()
                        .scaleY(1f)
                        .scaleX(1f)
                        .setDuration(200)
                        .start()
                }.start()

            clickListener()
        }

    }
}