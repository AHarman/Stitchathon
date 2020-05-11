package com.alexharman.stitchathon

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

interface IAreaDrawer {
    fun draw(canvas: Canvas, sourceArea: Rect, outputArea: Rect)

    val translationPaint: Paint
    val overallWidth: Int
    val overallHeight: Int
}