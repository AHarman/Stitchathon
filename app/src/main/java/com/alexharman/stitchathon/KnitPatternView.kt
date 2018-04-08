package com.alexharman.stitchathon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


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

    // TODO: Change some of these to val
    private var bitmapToDrawPaint: Paint
    private var patternDstRectangle: RectF? = null
    private var patternSrcRectangle: Rect? = null
    private var lockToScreen: Boolean
    private lateinit var currentView: Bitmap

    private val preferenceListener = MySharedPreferenceListener()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(context.getString(R.string.app_options_bg_colour_key), 0xFFFFFFFF)
        lockToScreen = prefs.getBoolean("lock_to_screen", false)
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
            updatePatternDstRectangle()
            updateCurrentView()
        }
    }

    private fun updatePatternSrcRectangle() {
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
        if (lockToScreen) centreOnNextStitch()

        // TODO: Left and top can be assigned here
        var left = 0
        var right: Int
        var top = 0
        var bottom: Int

        if (fitPatternWidth) {
            val ratio = patternBitmap.width.toFloat() / viewWidth.toFloat()
            right = patternBitmap.width
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
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return

        var left: Float
        var right: Float
        val top: Float
        val bottom: Float

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

    // TODO: Call this from updatePatternSrcRectangle
    private fun centreOnNextStitch() {
        Log.d("centre", "in centreOnNextStitch")
        val drawer = knitPatternDrawer ?: return
        val (centreX, centreY) = drawer.positionOfNextStitch()
        Log.d("centre", "fitPatternWidth: $fitPatternWidth")
        Log.d("centre", "x: $centreX, y: $centreY")
        Log.d("centre", "view: $viewHeight by $viewWidth")
        if (!fitPatternWidth && drawer.patternBitmap.width > viewWidth) {
            xOffset = min(max((centreX - viewWidth / 2).roundToInt(), 0), drawer.patternBitmap.width - viewWidth)
            Log.d("centre", "Update xOffset")
        }
        if (drawer.patternBitmap.height > viewHeight) {
            yOffset = min(max((centreY - viewHeight / 2).roundToInt(), 0), drawer.patternBitmap.height - viewHeight)
            Log.d("centre", "Update yOffset")
        }
        Log.d("centre", "xOffset: $xOffset")
        Log.d("centre", "yOffset: $yOffset")
    }

    internal fun zoomPattern() {
        if (knitPatternDrawer == null)
            return

        fitPatternWidth = !fitPatternWidth
        xOffset = 0

        updatePatternDstRectangle()
        updateCurrentView()
    }

    // TODO: maybe save paints and stitch bitmaps and pattern bitmaps to file or something.
    // TODO: Move to kotlin's fancy setters
    fun setPattern(knitPatternDrawer: KnitPatternDrawer) {
        this.knitPatternDrawer = knitPatternDrawer
        if (viewWidth > 0) {
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
        updatePatternSrcRectangle()
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(knitPatternDrawer?.patternBitmap, patternSrcRectangle, patternDstRectangle!!, bitmapToDrawPaint)
        invalidate()
    }

    internal fun scroll(distanceX: Float, distanceY: Float) {
        if (lockToScreen) return

        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
        val ratio = patternSrcRectangle!!.width().toFloat() / patternDstRectangle!!.width()
        xOffset = Math.min(Math.max(distanceX * ratio + xOffset, 0f), (patternBitmap.width - patternSrcRectangle!!.width()).toFloat()).toInt()
        yOffset = Math.min(Math.max(distanceY * ratio + yOffset, 0f), (patternBitmap.height - patternSrcRectangle!!.height()).toFloat()).toInt()
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
            if (key == "lock") {
                lockToScreen = sharedPreferences.getBoolean(key, lockToScreen)
                updateCurrentView()
            }
        }
    }
}

private fun SharedPreferences.getInt(key: String, default: Long): Int {
    return getInt(key, default.toInt())
}