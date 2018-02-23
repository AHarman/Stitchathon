package com.alexharman.stitchathon

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import java.util.*


class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var knitPatternDrawer: KnitPatternDrawer? = null

    private var fitPatternWidth = true
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var xOffset = 0
    private var yOffset = 0

    private var bitmapToDrawPaint: Paint
    private var patternDstRectangle: RectF? = null
    private var patternSrcRectangle: Rect? = null

    private val mGestureDetector: GestureDetector
    private val undoStack = Stack<Int>()

    private lateinit var currentView: Bitmap

    init {
        mGestureDetector = GestureDetector(this.context, MyGestureListener())
        bitmapToDrawPaint = Paint()
        bitmapToDrawPaint.isAntiAlias = true
        bitmapToDrawPaint.isFilterBitmap = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        viewHeight = h
        viewWidth = w
        currentView = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444)
        if (knitPatternDrawer != null) {
            updatePatternSrcRectangle()
            updatePatternDstRectangle()
            updateCurrentView()
        }
    }

    private fun updatePatternSrcRectangle() {
        if (knitPatternDrawer == null)
            return

        var left = 0
        var right: Int
        var top = 0
        var bottom: Int
        val patternBitmap = knitPatternDrawer!!.patternBitmap

        if (fitPatternWidth) {
            right = patternBitmap.width
            val ratio = patternBitmap.width.toFloat() / viewWidth.toFloat()
            bottom = Math.min(patternBitmap.height, (viewHeight.toFloat() * ratio).toInt())
        } else {
            right = Math.min(patternBitmap.width, viewWidth)
            bottom = Math.min(patternBitmap.height, viewHeight)
            left += xOffset
            right += xOffset
        }
        top += yOffset
        bottom += yOffset
        patternSrcRectangle = Rect(left, top, right, bottom)
    }

    private fun updatePatternDstRectangle() {
        if (knitPatternDrawer == null)
            return

        var left: Float
        var right: Float
        val top: Float
        val bottom: Float
        val patternBitmap = knitPatternDrawer!!.patternBitmap

        if (fitPatternWidth) {
            left = 0f
            top = 0f
            right = viewWidth.toFloat()
            val ratio = viewWidth.toFloat() / patternBitmap.width.toFloat()
            bottom = Math.min(viewHeight.toFloat(), patternBitmap.height * ratio)
        } else {
            left = 0f
            top = 0f
            right = Math.min(viewWidth, patternBitmap.width).toFloat()
            bottom = Math.min(viewHeight, patternBitmap.height).toFloat()
            if (patternBitmap.width < viewWidth) {
                left += ((viewWidth - patternBitmap.width) / 2).toFloat()
                right += ((viewWidth - patternBitmap.width) / 2).toFloat()
            }
        }
        patternDstRectangle = RectF(left, top, right, bottom)
    }

    private fun zoomPattern() {
        if (knitPatternDrawer == null)
            return

        fitPatternWidth = !fitPatternWidth
        xOffset = 0
        updatePatternSrcRectangle()
        updatePatternDstRectangle()
        updateCurrentView()
    }

    // TODO: maybe save paints and stitch bitmaps and pattern bitmaps to file or something.
    fun setPattern(knitPattern: KnitPattern) {
        setPattern(KnitPatternDrawer(knitPattern))
    }

    fun setPattern(knitPatternDrawer: KnitPatternDrawer) {
        this.knitPatternDrawer = knitPatternDrawer
        if (viewWidth > 0) {
            updatePatternSrcRectangle()
            updatePatternDstRectangle()
            currentView = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444)
            updateCurrentView()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(currentView, 0f, 0f, null)
    }

    private fun updateCurrentView() {
        val canvas = Canvas(currentView)
        if (viewWidth > patternDstRectangle!!.width() || viewHeight > patternDstRectangle!!.height()) {
            canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
        }
        canvas.drawBitmap(knitPatternDrawer?.patternBitmap, patternSrcRectangle, patternDstRectangle!!, bitmapToDrawPaint)
        invalidate()
    }

    private fun scroll(distanceX: Float, distanceY: Float) {
        if (knitPatternDrawer == null)
            return

        val patternBitmap = knitPatternDrawer!!.patternBitmap
        val ratio = patternSrcRectangle!!.width().toFloat() / patternDstRectangle!!.width()
        xOffset = Math.min(Math.max(distanceX * ratio + xOffset, 0f), (patternBitmap.width - patternSrcRectangle!!.width()).toFloat()).toInt()
        yOffset = Math.min(Math.max(distanceY * ratio + yOffset, 0f), (patternBitmap.height - patternSrcRectangle!!.height()).toFloat()).toInt()
        updatePatternSrcRectangle()
        updateCurrentView()
    }

    fun incrementOne() {
        if (knitPatternDrawer != null) {
            undoStack.push(1)
            knitPatternDrawer?.increment()
            (context as MainActivity).updateStitchCounter()
            updateCurrentView()
        }
    }

    fun incrementRow() {
        if (knitPatternDrawer == null )
            return

        undoStack.push(knitPatternDrawer!!.incrementRow())
        (context as MainActivity).updateStitchCounter()
        updateCurrentView()
    }

    fun undo() {
        if (undoStack.size == 0)
            return

        knitPatternDrawer?.undoStitches(undoStack.pop())
        (context as MainActivity).updateStitchCounter()
        updateCurrentView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            incrementOne()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            zoomPattern()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scroll(distanceX, distanceY)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }
}
