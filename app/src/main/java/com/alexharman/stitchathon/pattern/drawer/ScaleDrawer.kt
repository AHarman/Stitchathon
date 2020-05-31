package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas

interface ScaleDrawer {
    fun draw(canvas: Canvas, scale: Float)

    val overallWidth: Int
    val overallHeight: Int
}