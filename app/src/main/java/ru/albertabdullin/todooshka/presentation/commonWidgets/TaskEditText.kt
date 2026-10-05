package ru.albertabdullin.todooshka.presentation.commonWidgets

import android.content.Context
import android.util.AttributeSet
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

    var onSubmitTask: ((String, String) -> Unit)? = null
    var onDeleteTask: ((String) -> Unit)? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {

        val originalConnection = super.onCreateInputConnection(outAttrs) ?: return null

        return object : InputConnectionWrapper(originalConnection, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text?.toString() == "\n") {
                    val textParts = textParts()
                    if (textParts.isEmpty()) return super.commitText(text, newCursorPosition)
                    onSubmitTask?.invoke(textParts[0], textParts[1])
                    return true
                }
                return super.commitText(text, newCursorPosition)
            }

            override fun sendKeyEvent(event: KeyEvent): Boolean {

                val isFirstPos = (selectionStart == selectionEnd) && (selectionStart == 0)

                if (event.keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    isFirstPos
                ) {
                    val textParts = textParts()
                    if (textParts.isEmpty()) return super.sendKeyEvent(event)
                    onDeleteTask?.invoke(textParts.last())
                    return true
                }

                return super.sendKeyEvent(event)
            }
        }
    }

    private fun textParts(): List<String> {
        val taskDescriptionText = getText()?.toString() ?: ""
        val selectionStart = selectionStart
        val selectionEnd = selectionEnd
        if (selectionStart == -1 || selectionEnd == -1) {
            return emptyList()
        }
        val part1 = taskDescriptionText.substring(0, selectionStart)
        val part2 =
            taskDescriptionText.substring(selectionEnd.coerceAtMost(taskDescriptionText.length))
        return listOf(part1, part2)
    }
}