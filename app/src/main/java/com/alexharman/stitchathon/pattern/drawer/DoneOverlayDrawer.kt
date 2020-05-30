package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class DoneOverlayDrawer(private val stitchSize: Int, private val doneOverlayPaint: Paint): AreaDrawer {
    override fun draw(canvas: Canvas, sourceArea: Rect?, outputArea: Rect) {
        canvas.drawRect(outputArea, doneOverlayPaint)
    }

    override val overallWidth = stitchSize
    override val overallHeight = stitchSize
}