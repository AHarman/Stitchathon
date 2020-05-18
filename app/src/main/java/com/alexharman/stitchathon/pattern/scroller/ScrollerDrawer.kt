package com.alexharman.stitchathon.pattern.scroller

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.alexharman.stitchathon.pattern.drawer.AreaDrawer

// TODO: Add scaling, currently only works for scale = 1
class ScrollerDrawer(width: Int, height: Int, private val drawer: AreaDrawer) {
    var currentBitmap: Bitmap
        private set
    private var bufferBitmap: Bitmap
    private val viewPort = BoundedViewPort(Rect(0, 0, width, height), Rect(0, 0, drawer.overallWidth, drawer.overallHeight))

    init {
        currentBitmap = Bitmap.createBitmap(viewPort.width(), viewPort.height(), Bitmap.Config.ARGB_8888)
        bufferBitmap = Bitmap.createBitmap(viewPort.width(), viewPort.height(), Bitmap.Config.ARGB_8888)
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
        canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
        drawer.draw(canvas, viewPort.toRect(), Rect(0, 0, viewPort.width(), viewPort.height()))
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
            drawer.draw(canvas, sourceToDraw, canvasToDraw)
        } else if (offsetX > 0) {
            val sourceToDraw = Rect(viewPort.right - offsetX, viewPort.top, viewPort.right, viewPort.bottom)
            val canvasToDraw = Rect(viewPort.width() - offsetX, 0, viewPort.width(), viewPort.height())
            drawer.draw(canvas, sourceToDraw, canvasToDraw)
        }

        if (offsetY < 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.top, viewPort.right, viewPort.top - offsetY)
            val canvasToDraw = Rect(0, 0, viewPort.width(), -offsetY)
            drawer.draw(canvas, sourceToDraw, canvasToDraw)
        } else if (offsetY > 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.bottom - offsetY, viewPort.right, viewPort.bottom)
            val canvasToDraw = Rect(0, viewPort.height() - offsetY, viewPort.width(), viewPort.height())
            drawer.draw(canvas, sourceToDraw, canvasToDraw)
        }
    }
}