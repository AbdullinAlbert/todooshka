package ru.albertabdullin.todooshka.presentation.screen.commonWidgets

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
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
    var onSettingsClick: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.simple_task_form, this, true)
        findViewById<AppCompatImageButton>(R.id.simple_task_settings).setOnClickListener {
            onSettingsClick?.invoke()
        }

        val taskDescriptionEditText = findViewById<EditText>(R.id.simple_task_description_form)
        taskDescriptionEditText.doAfterTextChanged { editable ->
            if (editable != null) setButtonsEnableState(editable.toString())
        }
        taskDescriptionEditText.setOnEditorActionListener { view, _, event ->
            Log.d(SimpleTaskFormView::class.simpleName, "create task")
            return@setOnEditorActionListener if (event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && view.text.isNotBlank()) {
                    true
                } else false
            } else false
        }

        findViewById<MaterialCheckBox>(R.id.simple_task_checkBox).setOnCheckedChangeListener { _, bool ->
            onCheckedChangeListener(
                bool
            )
        }
    }

    private fun onCheckedChangeListener(checked: Boolean) {
        val descriptionForm = findViewById<EditText>(R.id.simple_task_description_form)
        findViewById<AppCompatImageButton>(R.id.simple_task_settings).isEnabled = !checked
        descriptionForm.isEnabled = !checked
        if (checked) {
            descriptionForm.paintFlags = descriptionForm.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            descriptionForm.paintFlags =
                descriptionForm.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun setButtonsEnableState(text: String) {
        val isEnabled = text.isNotEmpty()
        findViewById<CheckBox>(R.id.simple_task_checkBox).isEnabled = isEnabled
        findViewById<AppCompatImageButton>(R.id.simple_task_settings).isEnabled = isEnabled
    }

    fun setTask(task: Task) {
        findViewById<CheckBox>(R.id.simple_task_checkBox).isChecked = task.isCompleted
        findViewById<EditText>(R.id.simple_task_description_form).setText(task.description)
    }

    fun setOnSettingsButtonClickListener(clickListener: () -> Unit) {
        val button = findViewById<AppCompatImageButton>(R.id.simple_task_settings)

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
                button.imageTintList = ColorStateList.valueOf(animator.animatedValue as Int)
            }
        }
        iconColorAnimator.doOnEnd {
            button.imageTintList =
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
                button.backgroundTintList = ColorStateList.valueOf(animator.animatedValue as Int)
            }
        }

        button.setOnClickListener { view ->
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