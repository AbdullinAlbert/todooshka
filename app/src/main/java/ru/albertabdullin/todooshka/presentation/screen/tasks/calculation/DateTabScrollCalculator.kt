package ru.albertabdullin.todooshka.presentation.screen.tasks.calculation

class DateTabScrollCalculator {

    fun calculate(
        rvLeft: Int,
        rvRight: Int,
        itemLeft: Int,
        itemRight: Int
    ): ScrollResult {

        val distanceToItem = when {
            itemRight < rvLeft -> rvLeft - itemRight
            itemLeft > rvRight -> itemLeft - rvRight
            else -> 0
        }

        val rvWidth = rvRight - rvLeft
        val maxSmoothScrollDistance = rvWidth * 2

        return if (distanceToItem <= maxSmoothScrollDistance) {
            ScrollResult.SMOOTH_SCROLL
        } else {
            ScrollResult.INSTANT_SCROLL
        }
    }

    enum class ScrollResult {
        SMOOTH_SCROLL,
        INSTANT_SCROLL
    }

}
