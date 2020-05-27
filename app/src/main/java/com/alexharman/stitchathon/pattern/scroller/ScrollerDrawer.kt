package com.alexharman.stitchathon.pattern.scroller

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.alexharman.stitchathon.pattern.drawer.AreaDrawer
import kotlin.math.min

// TODO: Add scaling, currently only works for scale = 1
class ScrollerDrawer(private val viewWidth: Int, private val viewHeight: Int, private val drawer: AreaDrawer) {
    var currentBitmap: Bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        private set
    private var bufferBitmap: Bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
    private val viewPort = BoundedViewPort(
            Rect(0, 0, min(viewWidth, drawer.overallWidth), min(viewHeight, drawer.overallHeight)),
            Rect(0, 0, drawer.overallWidth, drawer.overallHeight))

    init {
        draw()
    }

    fun scroll(offsetX: Int, offsetY: Int) {
        val (adjustedOffsetX, adjustedOffsetY) = viewPort.offset(offsetX, offsetY)
        drawAfterOffset(adjustedOffsetX, adjustedOffsetY)
    }

    fun scrollTo(left: Int, top: Int) {
        val (adjustedOffsetX, adjustedOffsetY) = viewPort.offsetTo(left, top)
        drawAfterOffset(adjustedOffsetX, adjustedOffsetY)
    }

    fun draw() {
        val canvas = Canvas(currentBitmap)
        draw(canvas, viewPort.currentView, Rect(0, 0, viewPort.width(), viewPort.height()))
    }

    private fun draw(canvas: Canvas, source: Rect, destination: Rect) {
        drawer.draw(canvas, source, centerDrawingX(destination))
    }

    private fun centerDrawingX(drawingDestination: Rect): Rect {
        val offset =
                if (viewWidth > drawer.overallWidth)
                    (viewWidth - drawer.overallWidth) / 2
                else
                    0
        return Rect(drawingDestination).apply { offset(offset, 0)}
    }

    // Shift image and draw "new" segments
    private fun drawAfterOffset(offsetX: Int, offsetY: Int) {
        shiftExistingImage(offsetX, offsetY)
        drawNewScrollArea(offsetX, offsetY)
    }

    private fun shiftExistingImage(offsetX: Int, offsetY: Int) {
        // Draw the currentBitmap back onto itself, but translated slightly.
        val tempBitmap = currentBitmap
        currentBitmap = bufferBitmap
        bufferBitmap = tempBitmap
        Canvas(currentBitmap).drawBitmap(bufferBitmap, -offsetX.toFloat(), -offsetY.toFloat(), drawer.translationPaint)

        // Not strictly necessary if everything is working properly, but doing it for debug purposes
        Canvas(bufferBitmap).drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
    }

    // TODO: Currently drawing overlapping area twice
    private fun drawNewScrollArea(offsetX: Int, offsetY: Int) {
        val canvas = Canvas(currentBitmap)
        if (offsetX < 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.top, viewPort.left - offsetX, viewPort.bottom)
            val canvasToDraw = Rect(0, 0, offsetX, viewPort.height())
            draw(canvas, sourceToDraw, canvasToDraw)
        } else if (offsetX > 0) {
            val sourceToDraw = Rect(viewPort.right - offsetX, viewPort.top, viewPort.right, viewPort.bottom)
            val canvasToDraw = Rect(viewPort.width() - offsetX, 0, viewPort.width(), viewPort.height())
            draw(canvas, sourceToDraw, canvasToDraw)
        }
        if (offsetY < 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.top, viewPort.right, viewPort.top - offsetY)
            val canvasToDraw = Rect(0, 0, viewPort.width(), -offsetY)
            draw(canvas, sourceToDraw, canvasToDraw)
        } else if (offsetY > 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.bottom - offsetY, viewPort.right, viewPort.bottom)
            val canvasToDraw = Rect(0, viewPort.height() - offsetY, viewPort.width(), viewPort.height())
            draw(canvas, sourceToDraw, canvasToDraw)
        }
    }
}