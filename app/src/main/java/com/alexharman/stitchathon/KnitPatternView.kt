package com.alexharman.stitchathon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View


class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var backgroundColor: Int = 0
        @JvmName("_getBackgroundColour") get() {return field}
        @JvmName("_setBackgroundColour") set(color) { field = color }

    private var knitPatternDrawer: KnitPatternDrawer? = null

    private var fitPatternWidth = true
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var xOffset = 0
    private var yOffset = 0

    private var bitmapToDrawPaint: Paint
    private var patternDstRectangle: RectF? = null
    private var patternSrcRectangle: Rect? = null
    private lateinit var currentView: Bitmap

    private val preferenceListener = MySharedPreferenceListener()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(context.getString(R.string.app_options_bg_colour_key), 0xFFFFFFFF)
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

    internal fun zoomPattern() {
        if (knitPatternDrawer == null)
            return

        fitPatternWidth = !fitPatternWidth
        xOffset = 0
        updatePatternSrcRectangle()
        updatePatternDstRectangle()
        updateCurrentView()
    }

    // TODO: maybe save paints and stitch bitmaps and pattern bitmaps to file or something.
    // TODO: Move to kotlin's fancy setters
    fun setPattern(knitPatternDrawer: KnitPatternDrawer) {
        this.knitPatternDrawer = knitPatternDrawer
        if (viewWidth > 0) {
            updatePatternSrcRectangle()
            updatePatternDstRectangle()
            currentView = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444)
            updateCurrentView()
        }
    }

    fun clearPattern() {
        this.knitPatternDrawer = null
        Canvas(currentView).drawColor(backgroundColor)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(currentView, 0f, 0f, null)
    }

    fun updateCurrentView() {
        val canvas = Canvas(currentView)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(knitPatternDrawer?.patternBitmap, patternSrcRectangle, patternDstRectangle!!, bitmapToDrawPaint)
        invalidate()
    }

    internal fun scroll(distanceX: Float, distanceY: Float) {
        if (knitPatternDrawer == null)
            return

        val patternBitmap = knitPatternDrawer!!.patternBitmap
        val ratio = patternSrcRectangle!!.width().toFloat() / patternDstRectangle!!.width()
        xOffset = Math.min(Math.max(distanceX * ratio + xOffset, 0f), (patternBitmap.width - patternSrcRectangle!!.width()).toFloat()).toInt()
        yOffset = Math.min(Math.max(distanceY * ratio + yOffset, 0f), (patternBitmap.height - patternSrcRectangle!!.height()).toFloat()).toInt()
        updatePatternSrcRectangle()
        updateCurrentView()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    inner class MySharedPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == context.getString(R.string.app_options_bg_colour_key)) {
                backgroundColor = sharedPreferences.getInt(key,  backgroundColor)
                updateCurrentView()
            }
        }
    }
}

private fun SharedPreferences.getInt(key: String, default: Long): Int {
    return getInt(key, default.toInt())
}