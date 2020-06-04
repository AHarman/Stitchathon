package com.alexharman.stitchathon.pattern

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences
import com.alexharman.stitchathon.pattern.drawer.KnitPatternDrawer
import com.alexharman.stitchathon.pattern.scroller.ScrollerDrawer

class KnitPatternDisplayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var backgroundColor: Int = 0
        @JvmName("_getBackgroundColour") get
        @JvmName("_setBackgroundColour") set

    private var patternScroller: ScrollerDrawer? = null
    private var bitmapToDrawPaint: Paint = Paint()

    private var knitPatternDrawer: KnitPatternDrawer? = null
    private var pattern: KnitPattern? = null
    private var patternPrefs: KnitPatternPreferences? = null

    init {
        bitmapToDrawPaint.isAntiAlias = true
        bitmapToDrawPaint.isFilterBitmap = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        patternScroller = ScrollerDrawer(width, height, knitPatternDrawer ?: return)
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

    fun setPattern(pattern: KnitPattern?, preferences: KnitPatternPreferences?) {
        this.pattern = pattern
        this.patternPrefs = preferences
        if (pattern == null || preferences == null) {
            knitPatternDrawer = null
            patternScroller = null
            return
        }

        backgroundColor = preferences.backgroundColor
        knitPatternDrawer = KnitPatternDrawer(pattern, preferences)
        if (width > 0) {
            patternScroller = ScrollerDrawer(width, height, knitPatternDrawer ?: return)
            invalidate()
        }

    }

    fun scroll(distanceX: Float, distanceY: Float) {
        patternScroller?.scroll(distanceX.toInt(), distanceY.toInt())
        invalidate()
    }

    fun scrollToStitch(row: Int, col: Int) {
        val prefs = patternPrefs ?: return
        val x = ((col + 0.5) * (prefs.stitchSize + prefs.stitchPad)).toInt()
        val y = ((row + 0.5) * (prefs.stitchSize + prefs.stitchPad)).toInt()
        patternScroller?.centreOn(x, y)
        invalidate()
    }

    fun setZoomToPatternWidth() {
        patternScroller?.setZoomToPatternWidth()
        invalidate()
    }

    fun setZoom(zoom: Float) {
        patternScroller?.setZoom(zoom)
        invalidate()
    }
}

