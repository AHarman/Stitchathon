package com.alexharman.stitchathon.pattern.scroller

import android.graphics.*
import com.alexharman.stitchathon.pattern.drawer.AreaDrawer
import kotlin.math.min

// TODO: Add scaling, currently only works for scale = 1
class ScrollerDrawer(private val viewWidth: Int, private val viewHeight: Int, private val drawer: AreaDrawer) {
    var currentBitmap: Bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        private set
    private var bufferBitmap: Bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
    private var viewPort = createViewPort()
    private var baseDestination = createBaseDestRect()
    private val translationPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }

    private val currentZoom: Float
        get() = baseDestination.width().toFloat() / viewPort.currentView.width()

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
        drawer.draw(canvas, viewPort.currentView, baseDestination)
    }

    fun setZoomToPatternWidth() {
        setZoom(viewWidth.toFloat() / drawer.overallWidth.toFloat())
    }

    fun setZoom(zoom: Float) {
        viewPort = createViewPort(zoom)
        baseDestination = createBaseDestRect(zoom)
        Canvas(currentBitmap).drawColor(0x00000000, PorterDuff.Mode.CLEAR)
    }

    private fun createBaseDestRect(zoom: Float = 1F): Rect {
        val scaledDrawerWidth = (drawer.overallWidth * zoom).toInt()
        val scaledDrawerHeight = (drawer.overallHeight * zoom).toInt()
        val left =
                if (viewWidth > scaledDrawerWidth)
                    (viewWidth - scaledDrawerWidth) / 2
                else
                    0
        val right =
                if (viewWidth > scaledDrawerWidth)
                    left + scaledDrawerWidth
                else
                    viewWidth
        return Rect(left, 0, right, min(viewHeight, scaledDrawerHeight))
    }

    private fun createViewPort(zoom: Float = 1f): BoundedViewPort {
        return BoundedViewPort(
                Rect(
                        0,
                        0,
                        min((viewWidth / zoom).toInt(), drawer.overallWidth),
                        min((viewHeight /  zoom).toInt(), drawer.overallHeight)),
                Rect(0, 0, drawer.overallWidth, drawer.overallHeight))
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
        Canvas(currentBitmap).drawBitmap(bufferBitmap, -offsetX.toFloat(), -offsetY.toFloat(), translationPaint)

        // Not strictly necessary if everything is working properly, but doing it for debug purposes
        Canvas(bufferBitmap).drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
    }

    // TODO: Currently drawing overlapping area twice
    private fun drawNewScrollArea(offsetX: Int, offsetY: Int) {
        val canvas = Canvas(currentBitmap)
        if (offsetX < 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.top, viewPort.left - offsetX, viewPort.bottom)
            val destination = Rect(0, 0, offsetX, viewPort.height())
            if (destination.intersect(baseDestination))
                drawer.draw(canvas, sourceToDraw, destination)
        } else if (offsetX > 0) {
            val sourceToDraw = Rect(viewPort.right - offsetX, viewPort.top, viewPort.right, viewPort.bottom)
            val destination = Rect(viewPort.width() - offsetX, 0, viewPort.width(), viewPort.height())
            if (destination.intersect(baseDestination))
                drawer.draw(canvas, sourceToDraw, destination)
        }
        if (offsetY < 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.top, viewPort.right, viewPort.top - offsetY)
            val destination = Rect(0, 0, viewPort.width(), -offsetY)
            if (destination.intersect(baseDestination))
                drawer.draw(canvas, sourceToDraw, destination)
        } else if (offsetY > 0) {
            val sourceToDraw = Rect(viewPort.left, viewPort.bottom - offsetY, viewPort.right, viewPort.bottom)
            val destination = Rect(0, viewPort.height() - offsetY, viewPort.width(), viewPort.height())
            if (destination.intersect(baseDestination))
                drawer.draw(canvas, sourceToDraw, destination)
        }
    }
}