package com.alexharman.stitchathon

import android.graphics.Canvas
import android.graphics.Paint

class StitchDrawer(colour: Int, private val size: Int): IDrawer {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = colour
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    }
}