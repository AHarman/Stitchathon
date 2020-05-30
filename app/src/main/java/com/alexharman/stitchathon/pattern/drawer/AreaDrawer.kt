package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Rect

interface AreaDrawer {
    fun draw(canvas: Canvas, sourceArea: Rect?, outputArea: Rect)

    val overallWidth: Int
    val overallHeight: Int
}