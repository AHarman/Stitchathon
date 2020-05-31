package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint

class DoneOverlayDrawer(private val stitchSize: Int, private val doneOverlayPaint: Paint): ScaleDrawer {
    override val overallWidth = stitchSize
    override val overallHeight = stitchSize

    override fun draw(canvas: Canvas, scale: Float) {
        canvas.drawRect(0F, 0F, stitchSize * scale, stitchSize * scale, doneOverlayPaint)
    }
}