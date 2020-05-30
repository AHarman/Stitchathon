package com.alexharman.stitchathon.pattern.drawer

import android.graphics.*

class SplitStitchDrawer(firstColour: Int, secondColour: Int, stitchSize: Int): AreaDrawer {

    private val bitmap = createBitmap(firstColour, secondColour, stitchSize)

    override fun draw(canvas: Canvas, sourceArea: Rect?, outputArea: Rect) {
        canvas.drawBitmap(bitmap, sourceArea, outputArea, null)
    }

    override val overallWidth = stitchSize
    override val overallHeight = stitchSize

    private fun createBitmap(firstColour: Int, secondColour: Int, size: Int): Bitmap {
        val firstPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        firstPaint.color = firstColour
        firstPaint.style = Paint.Style.FILL
        secondPaint.color = secondColour
        secondPaint.style = Paint.Style.FILL
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val leftSide = Path()
        val rightSide: Path
        val startX = bitmap.width * 7 / 10
        val stopX = bitmap.width * 3 / 10
        val matrix = Matrix()

        leftSide.fillType = Path.FillType.EVEN_ODD
        leftSide.lineTo(startX - 1.0f, 0f)
        leftSide.lineTo(stopX - 1.0f, size.toFloat())
        leftSide.lineTo(0f, size.toFloat())
        leftSide.close()

        rightSide = Path(leftSide)
        matrix.postRotate(180f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        rightSide.transform(matrix)

        canvas.drawPath(rightSide, firstPaint)
        canvas.drawPath(leftSide, secondPaint)
        return bitmap
    }
}