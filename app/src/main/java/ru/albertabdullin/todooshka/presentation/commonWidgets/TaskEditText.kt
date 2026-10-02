package ru.albertabdullin.todooshka.presentation.commonWidgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText

class TaskEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onSubmitTask: (() -> Unit)? = null
    var onBackspaceInEmptyTask: (() -> Unit)? = null

    private var isDeletingHandled = false

    fun resetIsDeletingHandledFlag() {
        isDeletingHandled = false
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {

        val originalConnection = super.onCreateInputConnection(outAttrs) ?: return null

        return object : InputConnectionWrapper(originalConnection, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text?.toString() == "\n") {
                    onSubmitTask?.invoke()
                    return true
                }
                val isTextNotBlank = text?.toString()?.isNotBlank() ?: false
                if (isTextNotBlank) {
                    isDeletingHandled = false
                    return true
                }
                return super.commitText(text, newCursorPosition)
            }

            override fun sendKeyEvent(event: KeyEvent): Boolean {
                val isTextEmpty = text?.toString()?.isEmpty() ?: false

                if (event.keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    isTextEmpty
                ) {
                    return if (!isDeletingHandled) {
                        isDeletingHandled = true
                        onBackspaceInEmptyTask?.invoke()
                        true
                    } else {
                        false
                    }

                }

                return super.sendKeyEvent(event)
            }
        }
    }
}