package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import java.util.*

class KnitPatternDrawer(val knitPattern: KnitPattern, preferences: SharedPreferences) {
    private val stitchSize: Float
    private val stitchPad: Float
    private val colours: IntArray = IntArray(3)
    private var stitchBitmaps: HashMap<Stitch, Bitmap>
    private var stitchPaints: HashMap<Stitch, Paint>
    private var doneOverlayPaint: Paint
    private val undoStack = Stack<Int>()

    var patternBitmap: Bitmap
        private set

    init {
        colours[0] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_1, -1)
        colours[1] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_2, -1)
        colours[2] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_3, -1)
        stitchSize = preferences.getString(PreferenceKeys.STITCH_SIZE, "0").toFloat()
        stitchPad = preferences.getString(PreferenceKeys.STITCH_PAD, "0").toFloat()

        stitchPaints = createStitchPaints(knitPattern.stitchTypes)
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint.style = Paint.Style.FILL
        doneOverlayPaint.colorFilter = LightingColorFilter(0x00FFFFFF, 0x00888888)
        stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes)
        patternBitmap = createPatternBitmap(knitPattern)
    }

    private fun createPatternBitmap(knitPattern: KnitPattern): Bitmap {
        val bitmapWidth = (knitPattern.patternWidth * stitchSize + (knitPattern.patternWidth + 1) * stitchPad).toInt()
        val bitmapHeight = (knitPattern.numRows * stitchSize + (knitPattern.numRows + 1) * stitchPad).toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0x00, 1, 2, 3)
        drawPattern(canvas, knitPattern)
        return bitmap
    }

    private fun createStitchBitmaps(stitches: Array<Stitch>): HashMap<Stitch, Bitmap> {
        val stitchBitmaps = HashMap<Stitch, Bitmap>()
        var bitmap: Bitmap
        var bitmapWidth: Int

        for (stitch in stitches) {
            bitmapWidth = (stitchSize * stitch.width + (stitch.width - 1) * stitchPad).toInt()
            bitmap = Bitmap.createBitmap(bitmapWidth, stitchSize.toInt(), Bitmap.Config.ARGB_8888)
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
        leftSide.lineTo(stopX - 1.0f, stitchSize)
        leftSide.lineTo(0f, stitchSize)
        leftSide.close()

        rightSide = Path(leftSide)
        matrix.postRotate(180f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        rightSide.transform(matrix)

        canvas.drawPath(rightSide, stitchPaints[stitch.madeOf[0]])
        canvas.drawPath(leftSide, stitchPaints[stitch.madeOf[1]])
        return bitmap
    }

    private fun createStitchPaints(stitches: Array<Stitch>): HashMap<Stitch, Paint> {
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

    private fun drawPattern(canvas: Canvas, knitPattern: KnitPattern = this.knitPattern) {
        canvas.translate(0f, stitchPad)
        for (row in 0 until knitPattern.numRows) {
            canvas.save()
            canvas.translate(stitchPad, 0f)
            for (col in 0 until knitPattern.patternWidth) {
                val isDone = row < knitPattern.currentRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == 1 && col < knitPattern.nextStitchInRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == -1 && col > knitPattern.nextStitchInRow
                drawStitch(canvas, knitPattern.stitches[row][col], isDone)
                canvas.translate((stitchSize + stitchPad) * knitPattern.stitches[row][col].width, 0f)
            }
            canvas.restore()
            canvas.translate(0f, stitchSize + stitchPad)
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

    fun positionOfNextStitch(): Pair<Float, Float> {
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
