package com.alexharman.stitchathon.pattern.drawer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

class SplitStitchDrawer(firstColour: Int, secondColour: Int, stitchSize: Int): ScaleDrawer {

    override val overallWidth = stitchSize
    override val overallHeight = stitchSize
    private val leftSide: Path
    private val rightSide: Path
    private val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = firstColour
        style = Paint.Style.FILL
    }
    private val rightPaint = Paint(leftPaint).apply {
        color = secondColour
    }

    init {
        val (left, right) = createPaths(stitchSize)
        leftSide = left
        rightSide = right
    }

    override fun draw(canvas: Canvas, scale: Float) {
        val matrix = Matrix().apply { postScale(scale, scale) }
        val scaledLeft = Path(leftSide).apply { transform(matrix) }
        val scaledRight = Path(rightSide).apply { transform(matrix) }
        canvas.drawPath(scaledLeft, leftPaint)
        canvas.drawPath(scaledRight, rightPaint)
    }

    private fun createPaths(size: Int): Pair<Path, Path> {
        val leftSide = Path()
        val rightSide: Path
        val startX = size * 7 / 10
        val stopX = size * 3 / 10
        val matrix = Matrix()

        leftSide.fillType = Path.FillType.EVEN_ODD
        leftSide.lineTo(startX - 1f, 0f)
        leftSide.lineTo(stopX - 1f, size.toFloat())
        leftSide.lineTo(0f, size.toFloat())
        leftSide.close()

        rightSide = Path(leftSide)
        matrix.postRotate(180f, size / 2.0F, size / 2.0F)
        rightSide.transform(matrix)

        return Pair(leftSide, rightSide)
    }
}