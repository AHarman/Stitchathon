package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import java.util.*
import kotlin.math.min

class KnitPatternDrawer(val knitPattern: KnitPattern, preferences: SharedPreferences) {
    private val stitchSize: Int
    private val stitchPad: Int
    private val colours: IntArray = IntArray(3)
    private var lockToCentre: Boolean
    private var fitPatternWidth: Boolean

    private var stitchBitmaps: HashMap<Stitch, Bitmap>
    private var stitchPaints: HashMap<Stitch, Paint>
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private var doneOverlayPaint: Paint
    private val undoStack = Stack<Int>()

    val totalPatternHeight: Int
    val totalPatternWidth: Int
    lateinit var currentView: Rect
    lateinit var patternBitmap: Bitmap
        private set
    private lateinit var patternBitmapBuffer: Bitmap
    private val patternBitmapPaint: Paint

    constructor(knitPattern: KnitPattern, displayWidth: Int, displayHeight: Int, preferences: SharedPreferences): this(knitPattern, preferences) {
        setDisplayDimensions(displayWidth, displayHeight)
    }

    init {
        colours[0] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_1, -1)
        colours[1] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_2, -1)
        colours[2] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_3, -1)
        stitchSize = preferences.getString(PreferenceKeys.STITCH_SIZE, "0").toInt()
        stitchPad = preferences.getString(PreferenceKeys.STITCH_PAD, "0").toInt()
        totalPatternHeight = (knitPattern.stitches.size * (stitchSize + stitchPad) + stitchPad)
        totalPatternWidth = (knitPattern.patternWidth * (stitchSize + stitchPad) + stitchPad)
        lockToCentre = preferences.getBoolean(PreferenceKeys.LOCK_TO_CENTRE, false)
        fitPatternWidth = preferences.getBoolean(PreferenceKeys.FIT_PATTERN_WIDTH, false)

        stitchPaints = createStitchPaints(knitPattern.stitchTypes)
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint.style = Paint.Style.FILL
        doneOverlayPaint.colorFilter = LightingColorFilter(0x00FFFFFF, 0x00888888)
        stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes)
        patternBitmapPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }
    }

    fun setDisplayDimensions (displayWidth: Int, displayHeight: Int) {
        currentView = Rect(0, 0, displayWidth, displayHeight)
        patternBitmap = createPatternBitmap()
        patternBitmapBuffer = Bitmap.createBitmap(patternBitmap.width, patternBitmap.height, patternBitmap.config)
        drawPattern()
        if (lockToCentre) centreOnNextStitch()
    }

    private fun createPatternBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(currentView.width(), currentView.height(), Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0x00, 1, 2, 3)
        return bitmap
    }

    private fun createStitchBitmaps(stitches: Collection<Stitch>): HashMap<Stitch, Bitmap> {
        val stitchBitmaps = HashMap<Stitch, Bitmap>()
        var bitmap: Bitmap
        var bitmapWidth: Int

        for (stitch in stitches) {
            bitmapWidth = (stitchSize * stitch.width + (stitch.width - 1) * stitchPad)
            bitmap = Bitmap.createBitmap(bitmapWidth, stitchSize, Bitmap.Config.ARGB_8888)
            if (stitch.isSplit) {
                bitmap = createSplitStitchBitmap(stitch, bitmap)
            } else {
                Canvas(bitmap).drawPaint(stitchPaints[stitch])
            }
            stitchBitmaps[stitch] = bitmap
        }

        return stitchBitmaps
    }

    private fun createSplitStitchBitmap(stitch: Stitch, bitmap: Bitmap): Bitmap {
        val canvas = Canvas(bitmap)
        val leftSide = Path()
        val rightSide: Path
        val startX = bitmap.width * 7 / 10
        val stopX = bitmap.width * 3 / 10
        val matrix = Matrix()

        leftSide.fillType = Path.FillType.EVEN_ODD
        leftSide.lineTo(startX - 1.0f, 0f)
        leftSide.lineTo(stopX - 1.0f, stitchSize.toFloat())
        leftSide.lineTo(0f, stitchSize.toFloat())
        leftSide.close()

        rightSide = Path(leftSide)
        matrix.postRotate(180f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        rightSide.transform(matrix)

        canvas.drawPath(rightSide, stitchPaints[stitch.madeOf[0]])
        canvas.drawPath(leftSide, stitchPaints[stitch.madeOf[1]])
        return bitmap
    }

    private fun createStitchPaints(stitches: Collection<Stitch>): HashMap<Stitch, Paint> {
        var p: Paint
        val paints = HashMap<Stitch, Paint>()
        var colourCount = 0

        for (stitch in stitches.filter { !it.isSplit }) {
            p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = colours[colourCount]
            p.style = Paint.Style.FILL
            paints[stitch] = p
            colourCount++
        }
        return paints
    }

    private fun drawPattern(patternAreaToDraw: Rect = currentView) {
        val canvas = Canvas(patternBitmap)

        val firstRow = patternAreaToDraw.top / (stitchSize + stitchPad)
        val lastRow = min(patternAreaToDraw.bottom / (stitchSize + stitchPad) + 1, knitPattern.numRows - 1)
        val firstCol = patternAreaToDraw.left / (stitchSize + stitchPad)

        val bitmapAreaToDraw = findBitmapArea(patternAreaToDraw)

        canvas.drawRect(bitmapAreaToDraw, clearPaint)

        canvas.translate(bitmapAreaToDraw.left, bitmapAreaToDraw.top)
        canvas.translate(stitchPad, stitchPad)

        for (row in firstRow..lastRow) {
            val lastCol = min(patternAreaToDraw.right / (stitchSize + stitchPad) + 1, knitPattern.stitches[row].size - 1)
            canvas.save()

            for (col in firstCol..lastCol) {
                val isDone = row < knitPattern.currentRow ||
                        (row == knitPattern.currentRow && knitPattern.currentRowDirection == 1 && col < knitPattern.stitchesDoneInRow) ||
                        (row == knitPattern.currentRow && knitPattern.currentRowDirection == -1 && col > knitPattern.nextStitchInRow)
                drawStitch(canvas, knitPattern.stitches[row][col], isDone)
                canvas.translate(stitchSize + stitchPad, 0)
            }
            canvas.restore()
            canvas.translate(0, stitchSize + stitchPad)
        }
    }

    private fun findBitmapArea(knitPatternArea: Rect): Rect {
        val pad = Rect(
                -knitPatternArea.left % (stitchPad + stitchSize),
                -knitPatternArea.top % (stitchPad + stitchSize),
                (stitchPad + stitchSize) - (knitPatternArea.left % (stitchPad + stitchSize)),
                (stitchPad + stitchSize) - (knitPatternArea.top % (stitchPad + stitchSize)))
        val bitmapAreaToDraw = pad +
                Rect(knitPatternArea.left - currentView.left,
                        knitPatternArea.top - currentView.top,
                        knitPatternArea.right - currentView.left,
                        knitPatternArea.bottom - currentView.top)

        if (knitPatternArea.width() > totalPatternWidth) {
            bitmapAreaToDraw.inset((knitPatternArea.width() - totalPatternWidth) / 2, 0)
        }

        return bitmapAreaToDraw
    }

    private fun drawStitch(canvas: Canvas, stitch: Stitch, isDone: Boolean) {
        val b = stitchBitmaps[stitch]
        canvas.drawBitmap(b, 0f, 0f, if (isDone) doneOverlayPaint else null)
    }

    private fun drawNextStitch(isDone: Boolean) {
        val canvas = Canvas(patternBitmap)
        val stitch = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow]
        val (xTranslate, yTranslate) = positionOfNextStitchInCurrentView()
        canvas.translate(xTranslate, yTranslate)
        drawStitch(canvas, stitch, isDone)
    }

    fun undo() {
        if (undoStack.size == 0) {
            undoRow()
        } else {
            for (i in 0 until undoStack.pop()) {
                knitPattern.undoStitch()
                drawNextStitch(false)
            }
        }
        if (lockToCentre) centreOnNextStitch()
    }

    private fun undoRow() {
        do {
            knitPattern.undoStitch()
            drawNextStitch(false)
        } while (!knitPattern.isStartOfRow)
        if (lockToCentre) centreOnNextStitch()
    }

    fun increment(numStitches: Int = 1) {
        undoStack.push(numStitches)
        for (i in 0 until numStitches) {
            drawNextStitch(true)
            knitPattern.increment()
        }
        if(lockToCentre) centreOnNextStitch()
    }

    fun incrementRow() {
        increment(knitPattern.stitchesLeftInRow)
    }

    fun markStitchesTo(row: Int, col: Int) {
        Log.d("Go to", "markStitchesTo() - Row: $row, col: $col")
        undoStack.clear()
        while (row > knitPattern.currentRow) {
            incrementRow()
        }
        while (row < knitPattern.currentRow) {
            undoRow()
        }
        while (col > knitPattern.stitchesDoneInRow) {
            increment()
        }
        while (col < knitPattern.stitchesDoneInRow) {
            knitPattern.undoStitch()
            drawNextStitch(false)
        }
        undoStack.clear()
    }

    private fun positionOfNextStitchInPattern(): Pair<Int, Int> {
        val stitch = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow]
        val x =
                if (knitPattern.currentRowDirection == 1) {
                    knitPattern.currentDistanceInRow * (stitchPad + stitchSize) + stitchPad
                } else {
                    totalPatternWidth - (knitPattern.currentDistanceInRow + stitch.width) * (stitchPad + stitchSize)
                }
        val y = knitPattern.currentRow * (stitchPad + stitchSize) + stitchPad
        return Pair(x, y)
    }

    private fun positionOfNextStitchInCurrentView(): Pair<Int, Int> {
        val (patternX, patternY) = positionOfNextStitchInPattern()
        return Pair(patternX - currentView.left, patternY - currentView.top)
    }

    fun incrementBlock() {
        val stitchType = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow].type
        val currentRow = knitPattern.stitches[knitPattern.currentRow]
        var stitchesToDo = 0
        while (stitchesToDo + knitPattern.stitchesDoneInRow < currentRow.size &&
                currentRow[knitPattern.nextStitchInRow + stitchesToDo * knitPattern.currentRowDirection].type == stitchType) {
            stitchesToDo++
        }
        increment(stitchesToDo)
    }

    fun scroll(distanceX: Float, distanceY: Float) {
        if (!lockToCentre) shiftCurrentView(distanceX, distanceY)
    }

    private fun shiftCurrentView(distanceX: Float, distanceY: Float) {
        val shift = keepScrollWithinBounds(distanceX, distanceY)
        val canvas = Canvas(patternBitmapBuffer)
        currentView.offset(shift.first, shift.second)

        canvas.drawBitmap(patternBitmap, -shift.first.toFloat(), -shift.second.toFloat(), patternBitmapPaint)

        // Swap patternBitmap and patternBitmapBuffer
        // Saves drawing the latter back onto the first
        val tempBitmap = patternBitmap
        patternBitmap = patternBitmapBuffer
        patternBitmapBuffer = tempBitmap
        drawNewScrollArea(shift.first, shift.second)
    }

    // TODO: Overlapping in the corners
    private fun drawNewScrollArea(shiftX: Int, shiftY: Int) {
        if (shiftY < 0) {
            val topRect = Rect(currentView.left, currentView.top, currentView.right, currentView.top - shiftY)
            drawPattern(topRect)
        } else if (shiftY > 0) {
            val botRect = Rect(currentView.left, currentView.bottom - shiftY, currentView.right, currentView.bottom)
            drawPattern(botRect)
        }
        if (shiftX < 0) {
            val leftRect = Rect(currentView.left, currentView.top, currentView.left - shiftX, currentView.bottom)
            drawPattern(leftRect)
        } else if (shiftX > 0) {
            val rightRect = Rect(currentView.right - shiftX, currentView.top, currentView.right, currentView.bottom)
            drawPattern(rightRect)
        }
    }

    private fun keepScrollWithinBounds(shiftX: Float, shiftY: Float): Pair<Int, Int> {
        val myShiftX: Int = when {
            currentView.width() >= totalPatternWidth -> 0
            currentView.left + shiftX <= 0 -> -currentView.left
            currentView.right + shiftX > totalPatternWidth -> totalPatternWidth - currentView.right
            else -> shiftX.toInt()
        }

        val myShiftY: Int = when {
            currentView.height() >= totalPatternHeight -> 0
            currentView.top + shiftY <= 0 -> -currentView.top
            currentView.bottom + shiftY > totalPatternHeight -> totalPatternHeight - currentView.bottom
            else -> shiftY.toInt()
        }
        return Pair(myShiftX, myShiftY)
    }

    fun setLockToCentre(lockToCentre: Boolean) {
        this.lockToCentre = lockToCentre
        if (lockToCentre) centreOnNextStitch()
    }

    fun setFitPatternWidth(fitPatternWidth: Boolean) {
        this.fitPatternWidth = fitPatternWidth
        toggleFitPatternWidth()
    }

    private fun toggleFitPatternWidth() {
        val centreX = currentView.centerX()
        val centreY = currentView.centerY()
        val currentViewHeight: Int
        val currentViewWidth: Int

        if (fitPatternWidth) {
            val currentViewRatio = currentView.height().toFloat() / currentView.width().toFloat()
            currentViewHeight = min((currentViewRatio * totalPatternHeight).toInt(), patternBitmap.height)
            currentView.left = 0
            currentView.right = totalPatternWidth
        } else {
            currentViewWidth = min(currentView.width(), patternBitmap.width)
            currentViewHeight = min(currentView.height(), patternBitmap.height)
            currentView.left = centreX - currentViewWidth / 2
            currentView.right = centreX + currentViewWidth / 2
        }

        currentView.top = centreY - currentViewHeight / 2
        currentView.bottom = centreY + currentViewHeight / 2

        // Hacky, but ensures we're within bounds
        shiftCurrentView(0f, 0f)
    }

    private fun centreOnNextStitch() {
        val (stitchX, stitchY) = positionOfNextStitchInPattern()
        val shiftX = (stitchX - currentView.centerX()).toFloat()
        val shiftY = (stitchY - currentView.centerY()).toFloat()
        shiftCurrentView(shiftX, shiftY)
    }

    /* Extension functions */

    private fun Canvas.translate(x: Int, y: Int) {
        translate(x.toFloat(), y.toFloat())
    }

    private operator fun Rect.minus(other: Rect) =
            Rect(this.left - other.left,
                    this.top - other.top,
                    this.right - other.right,
                    this.bottom - other.bottom)
    private operator fun Rect.plus(other: Rect) =
            Rect(this.left + other.left,
                    this.top + other.top,
                    this.right + other.right,
                    this.bottom + other.bottom)
}
