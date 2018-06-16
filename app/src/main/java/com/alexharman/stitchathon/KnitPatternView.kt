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
                zoomSrcRect()
                scrollToNextStitch()
                updatePatternDstRectangle()
                currentView = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
                updateCurrentView()
            }
        }

    private var fitPatternWidth = true

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
        currentView = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        if (knitPatternDrawer != null) {
            updatePatternDstRectangle()
            zoomSrcRect()
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
            right = width.toFloat()
            val ratio = width.toFloat() / patternBitmap.width.toFloat()
            bottom = Math.min(height.toFloat(), patternBitmap.height * ratio)
        } else {
            left = 0f
            top = 0f
            right = Math.min(width, patternBitmap.width).toFloat()
            bottom = Math.min(height, patternBitmap.height).toFloat()
            if (patternBitmap.width < width) {
                left += ((width - patternBitmap.width) / 2).toFloat()
                right += ((width - patternBitmap.width) / 2).toFloat()
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
        canvas.drawBitmap(patternBitmap, 0f, 0f, bitmapToDrawPaint)
        invalidate()
    }

    internal fun scroll(distanceX: Float, distanceY: Float) {
        TODO("Not Migrated")
        if (lockToScreen || knitPatternDrawer == null) return
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
            if (key == PreferenceKeys.BACKGROUND_COLOUR) {
                backgroundColor = sharedPreferences.getInt(key,  backgroundColor)
                updateCurrentView()
            }
            if (key == PreferenceKeys.LOCK_TO_SCREEN) {
                lockToScreen = sharedPreferences.getBoolean(key, lockToScreen)
                updateCurrentView()
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

