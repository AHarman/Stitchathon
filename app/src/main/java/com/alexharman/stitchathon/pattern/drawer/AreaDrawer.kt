package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

interface AreaDrawer {
    fun draw(canvas: Canvas, sourceArea: Rect, outputArea: Rect)

    val translationPaint: Paint
    val overallWidth: Int
    val overallHeight: Int
}