package com.alexharman.stitchathon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View


class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var backgroundColor: Int = 0
        @JvmName("_getBackgroundColour") get() {return field}
        @JvmName("_setBackgroundColour") set(color) { field = color }

    var knitPatternDrawer: KnitPatternDrawer? = null
        set(value) {
            field = value
            if (width > 0) {
                knitPatternDrawer?.setDisplayDimensions(width, height)
                invalidate()
            }
        }

    // TODO: Change some of these to val
    private var bitmapToDrawPaint: Paint

    private val preferenceListener = MySharedPreferenceListener()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(PreferenceKeys.BACKGROUND_COLOUR, 0xFFFFFFFF)
        bitmapToDrawPaint = Paint()
        bitmapToDrawPaint.isAntiAlias = true
        bitmapToDrawPaint.isFilterBitmap = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (knitPatternDrawer != null) {
            knitPatternDrawer?.setDisplayDimensions(w, h)
            invalidate()
        }
    }

    fun clearPattern() {
        this.knitPatternDrawer = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
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
        }
    }

    /* Extension functions */

    private fun SharedPreferences.getInt(key: String, default: Long): Int {
        return getInt(key, default.toInt())
    }
}

