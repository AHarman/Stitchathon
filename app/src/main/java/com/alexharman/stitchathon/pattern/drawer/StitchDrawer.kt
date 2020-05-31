package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint

class StitchDrawer(colour: Int, stitchSize: Int): ScaleDrawer {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override val overallWidth = stitchSize
    override val overallHeight = stitchSize

    init {
        paint.color = colour
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas, scale: Float) {
        canvas.drawRect(0F, 0F, overallWidth * scale, overallHeight * scale, paint)
    }
}