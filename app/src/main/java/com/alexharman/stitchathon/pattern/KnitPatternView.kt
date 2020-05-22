package com.alexharman.stitchathon.pattern

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.PreferenceKeys
import com.alexharman.stitchathon.pattern.drawer.KnitPatternDrawer
import com.alexharman.stitchathon.pattern.scroller.ScrollerDrawer

class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var backgroundColor: Int = 0
        @JvmName("_getBackgroundColour") get() { return field }
        @JvmName("_setBackgroundColour") set(color) { field = color }

    private var patternScroller: ScrollerDrawer? = null
    private var bitmapToDrawPaint: Paint
    private val preferenceListener = MySharedPreferenceListener()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    private var knitPatternDrawer: KnitPatternDrawer? = null
    var pattern: KnitPattern? = null
        set(value) {
            field = value
            if (width > 0 && value != null) {
                val drawer = KnitPatternDrawer(value, prefs)
                patternScroller = ScrollerDrawer(width, height, drawer)
                invalidate()
            }
        }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(PreferenceKeys.BACKGROUND_COLOUR, 0xFFFFFFFF)
        bitmapToDrawPaint = Paint()
        bitmapToDrawPaint.isAntiAlias = true
        bitmapToDrawPaint.isFilterBitmap = true
    }

    fun scroll(distanceX: Float, distanceY: Float) {
        patternScroller?.scroll(distanceX.toInt(), distanceY.toInt())
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        patternScroller = ScrollerDrawer(width, height, knitPatternDrawer
                ?: return)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scroller = patternScroller ?: return
        scroller.draw()
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(scroller.currentBitmap, 0f, 0f, bitmapToDrawPaint)
    }

    // TODO: Check why do we need to do this
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

