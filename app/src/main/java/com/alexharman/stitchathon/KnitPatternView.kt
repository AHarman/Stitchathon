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
            if (knitPatternDrawer != null && width > 0) {
                value?.drawPattern()
            }
            invalidate()
        }

    private var bitmapToDrawPaint: Paint
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

        val knitPatternDrawer = this.knitPatternDrawer ?: return
        knitPatternDrawer.resize(width, height)
        knitPatternDrawer.drawPattern()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (knitPatternDrawer != null) {
            canvas.drawColor(backgroundColor)
            canvas.drawBitmap(knitPatternDrawer?.patternBitmap, 0f, 0f, null)
        } else {
            canvas.drawColor(backgroundColor)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    inner class MySharedPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == context.getString(R.string.app_options_bg_colour_key)) {
                backgroundColor = sharedPreferences.getInt(key, backgroundColor)
                knitPatternDrawer?.drawPattern()
            }
        }
    }

    private fun SharedPreferences.getInt(key: String, default: Long): Int {
        return getInt(key, default.toInt())
    }
}

