package com.alexharman.stitchathon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min


class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var backgroundColor: Int = 0
        @JvmName("_getBackgroundColour") get() {return field}
        @JvmName("_setBackgroundColour") set(color) { field = color }

    var knitPatternDrawer: KnitPatternDrawer? = null
        set(value) {
            field = value
            if (width > 0) {
                //TODO: something about this?
                invalidate()
            }
        }

    private var fitPatternWidth = true

    // TODO: Change some of these to val
    private var bitmapToDrawPaint: Paint
    private val patternSrcRectangle: Rect = Rect(0, 0, 0, 0)
    private var lockToScreen: Boolean

    private val preferenceListener = MySharedPreferenceListener()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(PreferenceKeys.BACKGROUND_COLOUR, 0xFFFFFFFF)
        lockToScreen = prefs.getBoolean(PreferenceKeys.LOCK_TO_SCREEN, false)
        fitPatternWidth = prefs.getBoolean(PreferenceKeys.FIT_PATTERN_WIDTH, false)
        zoomPattern(fitPatternWidth)
        bitmapToDrawPaint = Paint()
        bitmapToDrawPaint.isAntiAlias = true
        bitmapToDrawPaint.isFilterBitmap = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (knitPatternDrawer != null) {
            zoomSrcRect()
            invalidate()
        }
    }

    private fun scrollToNextStitch() {
//        val (newCentreX, newCentreY) = knitPatternDrawer?.positionOfNextStitch() ?: return
//        val centreX = patternSrcRectangle.centerX()
//        val centreY = patternSrcRectangle.centerY()
//        moveSrcRectAndCheckBounds(newCentreX.toInt() - centreX, newCentreY.toInt() - centreY)
    }

    private fun zoomSrcRect() {
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
        val centreX = patternSrcRectangle.centerX()
        val centreY = patternSrcRectangle.centerY()
        val srcRectHeight: Int
        val srcRectWidth: Int

        if (fitPatternWidth) {
            val ratio = height.toFloat() / width.toFloat()
            srcRectHeight = min((ratio * patternBitmap.width).toInt(), patternBitmap.height)
            patternSrcRectangle.left = 0
            patternSrcRectangle.right = patternBitmap.width
        } else {
            srcRectWidth = min(width, patternBitmap.width)
            srcRectHeight = min(height, patternBitmap.height)
            patternSrcRectangle.left = centreX - srcRectWidth / 2
            patternSrcRectangle.right = centreX + srcRectWidth / 2
        }

        patternSrcRectangle.top = centreY - srcRectHeight / 2
        patternSrcRectangle.bottom = centreY + srcRectHeight / 2
//        moveSrcRectAndCheckBounds(0, 0)
    }

    private fun zoomPattern(fitPatternWidth: Boolean) {
        this.fitPatternWidth = fitPatternWidth
        if (knitPatternDrawer == null)
            return
        zoomSrcRect()
        invalidate()
    }

    fun clearPattern() {
        this.knitPatternDrawer = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)

        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return

        if(lockToScreen) scrollToNextStitch()
        canvas.drawBitmap(patternBitmap, 0f, 0f, bitmapToDrawPaint)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    inner class MySharedPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PreferenceKeys.BACKGROUND_COLOUR) {
                backgroundColor = sharedPreferences.getInt(key,  backgroundColor)
                invalidate()
            }
            if (key == PreferenceKeys.LOCK_TO_SCREEN) {
                lockToScreen = sharedPreferences.getBoolean(key, lockToScreen)
                invalidate()
            }
            if (key == PreferenceKeys.FIT_PATTERN_WIDTH) {
                zoomPattern(sharedPreferences.getBoolean(key, false))
            }
        }
    }

    private fun SharedPreferences.getInt(key: String, default: Long): Int {
        return getInt(key, default.toInt())
    }
}

