package ru.albertabdullin.todooshka.presentation.screen.tasks.date_tab_scroll

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class CenteredDateTabSmoothScroller(
    context: Context
) : LinearSmoothScroller(context) {

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        val viewCenter = viewStart + (viewEnd - viewStart) / 2
        val recyclerViewCenter = boxStart + (boxEnd - boxStart) / 2

        return recyclerViewCenter - viewCenter
    }

    override fun calculateSpeedPerPixel(
        displayMetrics: DisplayMetrics
    ): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }

    private companion object {
        const val MILLISECONDS_PER_INCH = 150f
    }
}