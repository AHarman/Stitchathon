package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import java.util.*
import kotlin.math.min

class KnitPatternDrawer(val knitPattern: KnitPattern, displayWidth: Int, displayHeight: Int, preferences: SharedPreferences) {
    private val stitchSize: Int
    private val stitchPad: Int
    private val colours: IntArray = IntArray(3)
    private var stitchBitmaps: HashMap<Stitch, Bitmap>
    private var stitchPaints: HashMap<Stitch, Paint>
    private var doneOverlayPaint: Paint
    private val undoStack = Stack<Int>()

    private val totalPatternHeight: Int
    private val totalPatternWidth: Int
    private var currentViewHeight = displayHeight
    private var currentViewWidth = displayWidth
    private val currentView = Rect(0, 0, currentViewWidth, currentViewHeight)
    var patternBitmap: Bitmap
        private set

    init {
        colours[0] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_1, -1)
        colours[1] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_2, -1)
        colours[2] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_3, -1)
        stitchSize = preferences.getString(PreferenceKeys.STITCH_SIZE, "0").toInt()
        stitchPad = preferences.getString(PreferenceKeys.STITCH_PAD, "0").toInt()
        totalPatternHeight = (knitPattern.stitches.size * (stitchSize + stitchPad) + stitchPad)
        totalPatternWidth = (knitPattern.patternWidth * (stitchSize + stitchPad) + stitchPad)

        stitchPaints = createStitchPaints(knitPattern.stitchTypes)
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint.style = Paint.Style.FILL
        doneOverlayPaint.colorFilter = LightingColorFilter(0x00FFFFFF, 0x00888888)
        stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes)
        patternBitmap = createPatternBitmap()
        drawPattern()
    }

    private fun createPatternBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(currentViewWidth, currentViewHeight, Bitmap.Config.ARGB_4444)
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

    private fun drawPattern() {
        val canvas = Canvas(patternBitmap)
        val firstRow = currentView.top / (stitchSize + stitchPad)
        val lastRow = min(currentView.bottom / (stitchSize + stitchPad), knitPattern.numRows - 1)
        val firstCol = currentView.left / (stitchSize + stitchPad)
        canvas.translate(stitchPad, stitchPad)

        if (currentViewWidth > totalPatternWidth) {
            canvas.translate((currentViewWidth - totalPatternWidth).toFloat() / 2, 0f)
        }

        for (row in firstRow..lastRow) {
            val lastCol = min(currentView.right / (stitchSize + stitchPad), knitPattern.stitches[row].size - 1)
            canvas.save()

            for (col in firstCol..lastCol) {
                val isDone = row < knitPattern.currentRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == 1 && col < knitPattern.stitchesDoneInRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == -1 && col >= knitPattern.stitchesDoneInRow
                drawStitch(canvas, knitPattern.stitches[row][col], isDone)
                canvas.translate(stitchSize + stitchPad, 0)
            }
            canvas.restore()
            canvas.translate(0, stitchSize + stitchPad)
        }
    }

    private fun drawStitch(canvas: Canvas, stitch: Stitch, isDone: Boolean) {
        val b = stitchBitmaps[stitch]
        canvas.drawBitmap(b, 0f, 0f, if (isDone) doneOverlayPaint else null)
    }

    private fun drawNextStitch(isDone: Boolean) {
        val canvas = Canvas(patternBitmap)
        val stitch = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow]
        val xTranslate =
                if (knitPattern.rowDirection == 1) {
                    knitPattern.currentDistanceInRow * (stitchPad + stitchSize) + stitchPad
                } else {
                    patternBitmap.width - (knitPattern.currentDistanceInRow + stitch.width) * (stitchPad + stitchSize)
                }
        val yTranslate = knitPattern.currentRow * (stitchPad + stitchSize) + stitchPad
        canvas.translate(xTranslate, yTranslate)
        drawStitch(canvas, stitch, isDone)
    }

    fun undo() {
        if (undoStack.size == 0) {
            undoRow()
            return
        }
        for (i in 0 until undoStack.pop()) {
            knitPattern.undoStitch()
            drawNextStitch(false)
        }
    }

    private fun undoRow() {
        do {
            knitPattern.undoStitch()
            drawNextStitch(false)
        } while (!knitPattern.isStartOfRow)
    }

    fun increment(numStitches: Int = 1) {
        undoStack.push(numStitches)
        for (i in 0 until numStitches) {
            drawNextStitch(true)
            knitPattern.increment()
        }
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

    fun positionOfNextStitch(): Pair<Int, Int> {
        val x = knitPattern.currentDistanceInRow * (stitchSize + stitchPad) * knitPattern.rowDirection +
                if (knitPattern.rowDirection == -1) patternBitmap.width else 0
        val y = knitPattern.currentRow * (stitchSize + stitchPad)
        return Pair(x, y)
    }

    fun incrementBlock() {
        val stitchType = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow].type
        val currentRow = knitPattern.stitches[knitPattern.currentRow]
        var stitchesToDo = 0
        while (stitchesToDo + knitPattern.stitchesDoneInRow < currentRow.size &&
                currentRow[knitPattern.nextStitchInRow + stitchesToDo * knitPattern.rowDirection].type == stitchType) {
            stitchesToDo++
        }
        increment(stitchesToDo)
    }
}

private fun Canvas.translate(x: Int, y: Int) {
    translate(x.toFloat(), y.toFloat())
}