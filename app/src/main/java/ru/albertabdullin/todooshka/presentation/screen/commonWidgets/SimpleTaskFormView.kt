package ru.albertabdullin.todooshka.presentation.screen.commonWidgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.widget.doAfterTextChanged
import ru.albertabdullin.todooshka.R

class SimpleTaskFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var onSettingsClick: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.simple_task_form_item, this, true)
        findViewById<AppCompatImageButton>(R.id.simple_task_settings).setOnClickListener {
            onSettingsClick?.invoke()
        }
        findViewById<EditText>(R.id.simple_task_form).doAfterTextChanged { editable ->
            if (editable != null) setButtonsEnableState(editable.toString())
        }
    }

    private fun setButtonsEnableState(text: String) {
        val isEnabled = text.isNotEmpty()
        findViewById<CheckBox>(R.id.simple_task_checkBox).isEnabled = isEnabled
        findViewById<AppCompatImageButton>(R.id.simple_task_settings).isEnabled = isEnabled
    }
}