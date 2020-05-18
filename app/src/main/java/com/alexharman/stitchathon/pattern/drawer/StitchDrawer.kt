package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint
import com.alexharman.stitchathon.pattern.drawer.Drawer

class StitchDrawer(colour: Int, private val size: Int): Drawer {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = colour
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    }
}