package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.graphics.*
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import kotlin.collections.HashMap
import kotlin.math.min

class KnitPatternDrawer(val knitPattern: KnitPattern, preferences: SharedPreferences): IAreaDrawer {
    private val stitchSize: Int
    private val stitchPad: Int

    private var stitchDrawers: HashMap<Stitch, IDrawer>
    private val clearPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)//.apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val doneOverlayPaint: Paint

    override val overallHeight: Int
    override val overallWidth: Int
    override val translationPaint: Paint

    init {
        val colours = IntArray(3)
        colours[0] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_1, -1)
        colours[1] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_2, -1)
        colours[2] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_3, -1)

        stitchSize = preferences.getString(PreferenceKeys.STITCH_SIZE, "0")!!.toInt()
        stitchPad = preferences.getString(PreferenceKeys.STITCH_PAD, "0")!!.toInt()
        overallHeight = (knitPattern.stitches.size * (stitchSize + stitchPad) + stitchPad)
        overallWidth = (knitPattern.patternWidth * (stitchSize + stitchPad) + stitchPad)

        stitchDrawers = createStitchDrawers(knitPattern.stitchTypes, colours)
        clearPaint.color = 0xFF00FF00.toInt()
        clearPaint.style = Paint.Style.FILL
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint.style = Paint.Style.FILL
        doneOverlayPaint.colorFilter = LightingColorFilter(0x00FFFFFF, 0x00888888)
        translationPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }
    }

    private fun createStitchDrawers(stitches: Collection<Stitch>, colours: IntArray): HashMap<Stitch, IDrawer> {
        val drawers = HashMap<Stitch, IDrawer>()
        val stitchColours = HashMap<Stitch, Int>()

        // Assuming enough colours
        for ((index, stitch) in stitches.filter { !it.isSplit }.withIndex()) {
            stitchColours[stitch] = colours[index]
            drawers[stitch] = StitchDrawer(colours[index], stitchSize)
        }

        for (stitch in stitches.filter { it.isSplit }) {
            val firstColour = stitchColours[stitch.madeOf[0]]
            val secondColour = stitchColours[stitch.madeOf[1]]
            if (firstColour != null && secondColour != null)
                drawers[stitch] = SplitStitchDrawer(firstColour, secondColour, stitchSize)
        }

        return drawers
    }


    override fun draw(canvas: Canvas, sourceArea: Rect, outputArea: Rect) {
        val padding = findSourcePadding(sourceArea)
        val paddedSourceArea = padding + sourceArea
        val paddedOutputArea = padding + outputArea
        val firstRow = paddedSourceArea.top / (stitchSize + stitchPad)
        val lastRow = min(paddedSourceArea.bottom / (stitchSize + stitchPad), knitPattern.numRows - 1)
        val firstCol = paddedSourceArea.left / (stitchSize + stitchPad)
        var lastCol: Int
        var saveCount = canvas.save()

        canvas.drawRect(paddedOutputArea, clearPaint)
        canvas.translate(paddedOutputArea.left, paddedOutputArea.top)
        canvas.translate(stitchPad, stitchPad)

        for (row in firstRow..lastRow) {
            lastCol = min(paddedSourceArea.right / (stitchSize + stitchPad) + 1, knitPattern.stitches[row].size - 1)
            canvas.save()

            for (col in firstCol..lastCol) {
                val isDone = knitPattern.isStitchDone(row, col)
                stitchDrawers[knitPattern.stitches[row][col]]?.draw(canvas)
                canvas.translate(stitchSize + stitchPad, 0)
            }
            canvas.restore()
            canvas.translate(0, stitchSize + stitchPad)
        }

        canvas.restoreToCount(saveCount)
    }

    // Need to pad for half-in stitches
    private fun findSourcePadding(sourceArea: Rect): Rect {
        val stitchAndPadDistance = (stitchPad + stitchSize)
        // Only need to pad top/left
        return Rect(
                -sourceArea.left % stitchAndPadDistance,
                -sourceArea.top % stitchAndPadDistance,
                0,
                0)
    }

    /* Extension functions */
    private fun Canvas.translate(x: Int, y: Int) =
            this.translate(x.toFloat(), y.toFloat())

    private operator fun Rect.times(factor: Float) =
            Rect((this.left * factor).toInt(),
                    (this.top * factor).toInt(),
                    (this.right * factor).toInt(),
                    (this.bottom * factor).toInt())

    private operator fun Rect.plus(other: Rect) =
            Rect((this.left + other.left),
                    (this.top + other.top),
                    (this.right + other.right),
                    (this.bottom + other.bottom))
}
