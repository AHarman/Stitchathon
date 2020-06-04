package com.alexharman.stitchathon.pattern.scroller

import android.graphics.Rect

class BoundedViewPort(val currentView: Rect, val viewportBounds: Rect) {

    // Mimicking the Rect class to avoid confusion
    val top get() = currentView.top
    val left get() = currentView.left
    val bottom get() = currentView.bottom
    val right get() = currentView.right
    fun height() = currentView.height()
    fun width() = currentView.width()

    fun toRect(): Rect {
        return Rect(currentView)
    }

    private fun keepOffsetWithinBounds(shiftX: Int, shiftY: Int): Pair<Int, Int> {
        val myShiftX: Int = when {
            currentView.left + shiftX < viewportBounds.left -> viewportBounds.left - currentView.left
            currentView.right + shiftX > viewportBounds.right -> viewportBounds.right - currentView.right
            else -> shiftX
        }

        val myShiftY: Int = when {
            currentView.top + shiftY < viewportBounds.top -> viewportBounds.top - currentView.top
            currentView.bottom + shiftY > viewportBounds.bottom -> viewportBounds.bottom - currentView.bottom
            else -> shiftY
        }

        return Pair(myShiftX, myShiftY)
    }

    fun offsetTo(newLeft: Int, newTop: Int): Pair<Int, Int> {
        return offset(newLeft - currentView.left, newTop - currentView.top)
    }

    fun offset(shiftX: Int, shiftY: Int): Pair<Int, Int> {
        val (inBoundsX, inBoundsY) = keepOffsetWithinBounds(shiftX, shiftY)
        currentView.offset(inBoundsX, inBoundsY)
        return Pair (inBoundsX, inBoundsY)
    }
}