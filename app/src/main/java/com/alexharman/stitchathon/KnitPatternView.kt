package com.alexharman.stitchathon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch

import java.util.HashMap
import java.util.Stack


class KnitPatternView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var pattern: KnitPattern? = null

    // So far only built for two-color double knits.
    private val stitchSize = 10f
    private val stitchPad = 2f

    private var fitPatternWidth = true
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    internal var xOffset = 0
    internal var yOffset = 0

    // TODO: Create colour changer and pass this in from preferences
    private val backgroundColor = -0x7f7f80
    private val doneOverlayColor = -0x7f000001
    internal var colours = intArrayOf(-0x10000, -0xffff01, -0xff0100)

    internal lateinit var stitchBitmaps: HashMap<Stitch, Bitmap>
    internal lateinit var stitchPaints: HashMap<Stitch, Paint>
    private var bitmapToDrawPaint: Paint? = null
    private var doneOverlayPaint: Paint? = null
    internal var patternBitmap: Bitmap? = null

    //TODO: Rename "currentView" or something.
    internal lateinit var bitmapToDraw: Bitmap

    private var patternDstRectangle: RectF? = null
    private var patternSrcRectangle: Rect? = null

    private val undoStack = Stack<Int>()

    private val mGestureDetector: GestureDetector

    init {
        mGestureDetector = GestureDetector(this.context, gestureListener())
    }

    fun createPatternBitmap(knitPattern: KnitPattern): Bitmap {
        val bitmapWidth = (knitPattern.patternWidth * stitchSize + (knitPattern.patternWidth + 1) * stitchPad).toInt()
        val bitmapHeight = (knitPattern.numRows * stitchSize + (knitPattern.numRows + 1) * stitchPad).toInt()

        // TODO: Maybe don't set properties here
        this.stitchPaints = createPaints(knitPattern.stitchTypes)
        this.stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        drawPattern(canvas, knitPattern)

        return bitmap
    }

    // TODO: Variable width
    private fun createStitchBitmaps(stitches: Array<Stitch>): HashMap<Stitch, Bitmap> {
        val stitchBitmaps = HashMap<Stitch, Bitmap>()
        var bitmap: Bitmap
        var bitmapWidth: Int
        var currentStitch: Stitch

        for (i in stitches.indices) {
            currentStitch = stitches[i]
            if (currentStitch.isSplit) {
                bitmap = createSplitStitchBitmap(currentStitch)
            } else {
                bitmapWidth = (stitchSize * currentStitch.width + (currentStitch.width - 1) * stitchPad).toInt()
                bitmap = Bitmap.createBitmap(bitmapWidth, stitchSize.toInt(), Bitmap.Config.ARGB_8888)
                Canvas(bitmap).drawPaint(stitchPaints[currentStitch])
            }
            stitchBitmaps[stitches[i]] = bitmap
        }

        return stitchBitmaps
    }

    // TODO: Variable width
    // We're going to assume that there's only 2 colours
    private fun createSplitStitchBitmap(stitch: Stitch): Bitmap {
        val bitmapWidth = (stitchSize * stitch.width + (stitch.width - 1) * stitchPad).toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, stitchSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val leftSide: Path
        val rightSide: Path

        // Can't think of better names for these
        // Basically they define the diagonal
        val startX = stitchSize * 7 / 10
        val stopX = stitchSize * 3 / 10
        val matrix: Matrix

        leftSide = Path()
        leftSide.fillType = Path.FillType.EVEN_ODD
        leftSide.lineTo(startX - 1.0f, 0f)
        leftSide.lineTo(stopX - 1.0f, stitchSize)
        leftSide.lineTo(0f, stitchSize)
        leftSide.close()

        rightSide = Path(leftSide)
        matrix = Matrix()
        matrix.postRotate(180f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        rightSide.transform(matrix)

        canvas.drawPath(rightSide, stitchPaints[stitch.madeOf[0]])
        canvas.drawPath(leftSide, stitchPaints[stitch.madeOf[1]])
        return bitmap
    }

    private fun createPaints(stitches: Array<Stitch>): HashMap<Stitch, Paint> {
        var p: Paint
        val paints = HashMap<Stitch, Paint>()
        var colourCount = 0

        for (stitch in stitches) {
            if (!stitch.isSplit) {
                p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.color = colours[colourCount]
                p.style = Paint.Style.FILL
                paints[stitch] = p
                colourCount++
            }
        }

        // TODO: Split stitch paints and others into another function
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint!!.color = doneOverlayColor
        doneOverlayPaint!!.style = Paint.Style.FILL
        bitmapToDrawPaint = Paint()
        bitmapToDrawPaint!!.isAntiAlias = true
        bitmapToDrawPaint!!.isFilterBitmap = true

        return paints
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewHeight = h
        viewWidth = w
        bitmapToDraw = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444)
        if (pattern != null) {
            updatePatternSrcRectangle()
            updatePatternDstRectangle()
            updateBitmapToDraw()
        }
        invalidate()
    }

    private fun updatePatternSrcRectangle() {
        var left: Int
        var right: Int
        var top: Int
        var bottom: Int
        if (fitPatternWidth) {
            left = 0
            right = patternBitmap!!.width
            top = 0
            val ratio = patternBitmap!!.width.toFloat() / viewWidth.toFloat()
            bottom = Math.min(patternBitmap!!.height, (viewHeight.toFloat() * ratio).toInt())
        } else {
            left = 0
            right = Math.min(patternBitmap!!.width, viewWidth)
            top = 0
            bottom = Math.min(patternBitmap!!.height, viewHeight)
            left += xOffset
            right += xOffset
        }
        top += yOffset
        bottom += yOffset
        patternSrcRectangle = Rect(left, top, right, bottom)
    }

    private fun updatePatternDstRectangle() {
        var left: Float
        var right: Float
        val top: Float
        val bottom: Float
        if (fitPatternWidth) {
            left = 0f
            top = 0f
            right = viewWidth.toFloat()
            val ratio = viewWidth.toFloat() / patternBitmap!!.width.toFloat()
            bottom = Math.min(viewHeight.toFloat(), patternBitmap!!.height * ratio)
        } else {
            left = 0f
            top = 0f
            right = Math.min(viewWidth, patternBitmap!!.width).toFloat()
            bottom = Math.min(viewHeight, patternBitmap!!.height).toFloat()
            if (patternBitmap!!.width < viewWidth) {
                left += ((viewWidth - patternBitmap!!.width) / 2).toFloat()
                right += ((viewWidth - patternBitmap!!.width) / 2).toFloat()
            }
        }
        patternDstRectangle = RectF(left, top, right, bottom)
    }

    private fun zoomPattern() {
        if (pattern == null) {
            return
        }
        fitPatternWidth = !fitPatternWidth
        xOffset = 0
        updatePatternSrcRectangle()
        updatePatternDstRectangle()
        updateBitmapToDraw()
        invalidate()
    }

    private fun scroll(distanceX: Float, distanceY: Float) {
        if (patternBitmap == null) {
            return
        }
        val ratio = patternSrcRectangle!!.width().toFloat() / patternDstRectangle!!.width()
        xOffset = Math.min(Math.max(distanceX * ratio + xOffset, 0f), (patternBitmap!!.width - patternSrcRectangle!!.width()).toFloat()).toInt()
        yOffset = Math.min(Math.max(distanceY * ratio + yOffset, 0f), (patternBitmap!!.height - patternSrcRectangle!!.height()).toFloat()).toInt()
        updatePatternSrcRectangle()
        updateBitmapToDraw()
        invalidate()
    }

    fun incrementOne() {
        if (pattern == null || pattern!!.isFinished) {
            return
        }
        undoStack.push(1)
        markStitchesDone(1)
        pattern!!.increment()
        (context as MainActivity).updateStitchCounter()
        invalidate()
    }

    fun incrementRow() {
        if (pattern == null || pattern!!.isFinished) {
            return
        }
        markStitchesDone(pattern!!.stitchesLeftInRow)
        undoStack.push(pattern!!.incrementRow())
        (context as MainActivity).updateStitchCounter()
        invalidate()
    }

    // TODO: maybe save paints and stitch bitmaps to file or something.
    @JvmOverloads
    fun setPattern(pattern: KnitPattern, bitmap: Bitmap? = null) {
        this.pattern = pattern
        stitchPaints = createPaints(pattern.stitchTypes)
        stitchBitmaps = createStitchBitmaps(pattern.stitchTypes)
        if (bitmap == null) {
            patternBitmap = createPatternBitmap(pattern)
        } else {
            this.patternBitmap = bitmap
        }

        if (viewWidth > 0) {
            updatePatternSrcRectangle()
            updatePatternDstRectangle()
            bitmapToDraw = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444)
            updateBitmapToDraw()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pattern == null) {
            return
        }
        canvas.drawBitmap(bitmapToDraw, 0f, 0f, null)
    }

    private fun drawPattern(canvas: Canvas, knitPattern: KnitPattern? = this.pattern) {
        canvas.translate(0f, stitchPad)
        for (row in 0 until knitPattern!!.numRows) {
            canvas.save()
            canvas.translate(stitchPad, 0f)
            for (col in 0 until knitPattern.patternWidth) {
                val isDone = row < knitPattern.currentRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == 1 && col < knitPattern.nextStitchInRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == -1 && col > knitPattern.nextStitchInRow
                drawStitch(canvas, knitPattern.stitches[row][col], isDone)
                canvas.translate(stitchSize + stitchPad, 0f)
            }
            canvas.restore()
            canvas.translate(0f, stitchSize + stitchPad)
        }
    }

    private fun drawStitch(canvas: Canvas, stitch: Stitch, isDone: Boolean) {
        val b = stitchBitmaps[stitch]
        canvas.drawBitmap(b, 0f, 0f, if (isDone) doneOverlayPaint else null)
    }

    // TODO: Fix so works beyond end of row
    // TODO: I think this breaks if stitches aren't uniform width. Fix that.
    // TODO: Just redo this function. There's too many problems. Break it down into "mark one stitch done" and "mark n stitches", make it use drawStitch() function. See SetColorFilter for the paint

    // Only works until end of row, don't use beyond that
    private fun markStitchesDone(numStitches: Int) {
        val row = pattern!!.currentRow
        val col = pattern!!.nextStitchInRow
        val (_, width) = pattern!!.stitches[row][col]
        val canvas = Canvas(patternBitmap!!)
        val yTranslate = stitchPad + row * (stitchPad + stitchSize)
        var xTranslate: Float
        if (pattern!!.rowDirection == 1) {
            xTranslate = stitchPad + pattern!!.currentDistanceInRow * (stitchPad + stitchSize)
            xTranslate += (width - 1) * (stitchSize + stitchPad)
        } else {
            xTranslate = patternBitmap!!.width - (pattern!!.currentDistanceInRow + 1) * (stitchPad + stitchSize)
        }
        canvas.translate(xTranslate, yTranslate)
        xTranslate = pattern!!.rowDirection * (stitchPad + stitchSize)

        for (i in 0 until numStitches) {
            canvas.drawRect(0f, 0f, stitchSize, stitchSize, doneOverlayPaint!!)
            canvas.translate(xTranslate, 0f)
        }
        updateBitmapToDraw()
    }

    private fun updateBitmapToDraw() {
        val canvas = Canvas(bitmapToDraw)
        if (viewWidth > patternDstRectangle!!.width() || viewHeight > patternDstRectangle!!.height()) {
            canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
        }
        canvas.drawBitmap(patternBitmap!!, patternSrcRectangle, patternDstRectangle!!, bitmapToDrawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

    fun undo() {
        if (undoStack.size == 0) {
            return
        }
        val stitchesToUndo = undoStack.pop()
        val canvas = Canvas(patternBitmap!!)
        var lastStitch: Stitch
        var xTranslate: Float
        var yTranslate: Float

        for (i in 0 until stitchesToUndo) {
            pattern!!.undoStitch()
            lastStitch = pattern!!.stitches[pattern!!.currentRow][pattern!!.nextStitchInRow]
            if (pattern!!.rowDirection == 1) {
                xTranslate = pattern!!.currentDistanceInRow * (stitchPad + stitchSize) + stitchPad
            } else {
                xTranslate = patternBitmap!!.width - (pattern!!.currentDistanceInRow + 1) * (stitchPad + stitchSize)
            }
            yTranslate = pattern!!.currentRow * (stitchPad + stitchSize) + stitchPad
            canvas.translate(xTranslate, yTranslate)
            drawStitch(canvas, lastStitch, false)
            canvas.matrix = null
        }
        updateBitmapToDraw()
        (context as MainActivity).updateStitchCounter()
        invalidate()
    }

    private inner class gestureListener : GestureDetector.SimpleOnGestureListener() {
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

private fun Canvas.drawColor(color: Long) { drawColor(color.toInt())}