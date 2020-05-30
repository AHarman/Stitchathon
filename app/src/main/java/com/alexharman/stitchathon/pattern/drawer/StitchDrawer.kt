package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class StitchDrawer(colour: Int, stitchSize: Int): AreaDrawer {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override val overallWidth = stitchSize
    override val overallHeight = stitchSize

    init {
        paint.color = colour
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas, sourceArea: Rect?, outputArea: Rect) {
        canvas.drawRect(outputArea, paint)
    }
}