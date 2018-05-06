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

    private var knitPatternDrawer: KnitPatternDrawer? = null

    private var fitPatternWidth = true
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0

    // TODO: Change some of these to val
    private var bitmapToDrawPaint: Paint
    private var patternDstRectangle: RectF? = null
    private val patternSrcRectangle: Rect = Rect(0, 0, 0, 0)
    private var lockToScreen: Boolean
    private lateinit var currentView: Bitmap

    private val preferenceListener = MySharedPreferenceListener()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
        backgroundColor = prefs.getInt(context.getString(R.string.app_options_bg_colour_key), 0xFFFFFFFF)
        lockToScreen = prefs.getBoolean("lock_to_screen", false)
        fitPatternWidth = prefs.getBoolean("fit_pattern_width", false)
        zoomPattern(fitPatternWidth)
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

    private fun moveSrcRectAndCheckBounds(shiftX: Int, shiftY: Int) {
        Log.d("Move", "$shiftX, $shiftY")
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
        var myShiftX = shiftX
        var myShiftY = shiftY

        if (fitPatternWidth) {
            myShiftX = 0
        } else {
            if (patternSrcRectangle.left + shiftX < 0) {
                myShiftX = -patternSrcRectangle.left
            } else if (patternSrcRectangle.right + shiftX > patternBitmap.width) {
                myShiftX = patternBitmap.width - patternSrcRectangle.right
            }
        }

        if (patternSrcRectangle.top + shiftY < 0) {
            myShiftY = -patternSrcRectangle.top
        } else if (patternSrcRectangle.bottom + shiftY > patternBitmap.height) {
            myShiftY = patternBitmap.height - patternSrcRectangle.bottom
        }

        patternSrcRectangle.left += myShiftX
        patternSrcRectangle.right += myShiftX
        patternSrcRectangle.top += myShiftY
        patternSrcRectangle.bottom += myShiftY
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

    private fun scrollToNextStitch() {
        val (newCentreX, newCentreY) = knitPatternDrawer?.positionOfNextStitch() ?: return
        val centreX = patternSrcRectangle.centerX()
        val centreY = patternSrcRectangle.centerY()
        moveSrcRectAndCheckBounds(newCentreX.toInt() - centreX, newCentreY.toInt() - centreY)
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
        moveSrcRectAndCheckBounds(0, 0)
    }

    private fun zoomPattern(fitPatternWidth: Boolean) {
        this.fitPatternWidth = fitPatternWidth
        if (knitPatternDrawer == null)
            return
        zoomSrcRect()
        updatePatternDstRectangle()
        updateCurrentView()
    }

    // TODO: maybe save paints and stitch bitmaps and pattern bitmaps to file or something.
    // TODO: Move to kotlin's fancy setters
    fun setPattern(knitPatternDrawer: KnitPatternDrawer) {
        this.knitPatternDrawer = knitPatternDrawer
        if (viewWidth > 0) {
            //TODO: something about this?
            zoomSrcRect()
            scrollToNextStitch()
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
        val patternBitmap = knitPatternDrawer?.patternBitmap ?: return
        val canvas = Canvas(currentView)

        if(lockToScreen) scrollToNextStitch()
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(patternBitmap, patternSrcRectangle, patternDstRectangle!!, bitmapToDrawPaint)
        invalidate()
    }

    internal fun scroll(distanceX: Float, distanceY: Float) {
        if (lockToScreen) return
        val ratio = patternSrcRectangle.width().toFloat() / patternDstRectangle!!.width()

        moveSrcRectAndCheckBounds((distanceX * ratio).toInt(),(distanceY * ratio).toInt())
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
            if (key == "fit_pattern_width") {
                zoomPattern(sharedPreferences.getBoolean(key, false))
            }
        }
    }

    private fun SharedPreferences.getInt(key: String, default: Long): Int {
        return getInt(key, default.toInt())
    }
}

