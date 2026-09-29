package ru.albertabdullin.todooshka.presentation.screen.commonWidgets

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

class TaskEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onSubmitTask: (() -> Unit)? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {

        val originalConnection = super.onCreateInputConnection(outAttrs) ?: return null

        return object : InputConnectionWrapper(originalConnection, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text?.toString() == "\n") {
                    onSubmitTask?.invoke()
                    return true
                }
                return super.commitText(text, newCursorPosition)
            }
        }
    }
}