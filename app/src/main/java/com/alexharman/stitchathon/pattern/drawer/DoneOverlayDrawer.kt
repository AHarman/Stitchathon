package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class DoneOverlayDrawer(private val stitchSize: Int, private val doneOverlayPaint: Paint): Drawer {
    override fun draw(canvas: Canvas) {
        canvas.drawRect(Rect(0, 0, stitchSize, stitchSize), doneOverlayPaint)
    }
}