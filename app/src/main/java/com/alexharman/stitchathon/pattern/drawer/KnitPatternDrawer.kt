package com.alexharman.stitchathon.pattern.drawer

import android.content.SharedPreferences
import android.graphics.*
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import com.alexharman.stitchathon.repository.PreferenceKeys
import kotlin.math.min

// TODO: Pass in colors rather than SharedPreferences object
class KnitPatternDrawer(val knitPattern: KnitPattern, preferences: SharedPreferences): AreaDrawer {
    private val stitchSize: Int
    private val stitchPad: Int

    private var stitchDrawers: HashMap<Stitch, AreaDrawer>
    private val doneOverlayDrawer: DoneOverlayDrawer
    private val clearPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override val overallHeight: Int
    override val overallWidth: Int

    init {
        val colours = IntArray(3)
        colours[0] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_1, -1)
        colours[1] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_2, -1)
        colours[2] = preferences.getInt(PreferenceKeys.STITCH_COLOUR_3, -1)

        stitchSize = preferences.getString(PreferenceKeys.STITCH_SIZE, "0")!!.toInt()
        stitchPad = preferences.getString(PreferenceKeys.STITCH_PAD, "0")!!.toInt()
        overallHeight = (knitPattern.stitches.size * (stitchSize + stitchPad) + stitchPad)
        overallWidth = (knitPattern.patternWidth * (stitchSize + stitchPad) + stitchPad)

        val doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x88FFFFFF.toInt()
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }

        doneOverlayDrawer = DoneOverlayDrawer(stitchSize, doneOverlayPaint)
        stitchDrawers = createStitchDrawers(knitPattern.stitchTypes, colours)
    }

    private fun createStitchDrawers(stitches: Collection<Stitch>, colours: IntArray): HashMap<Stitch, AreaDrawer> {
        val drawers = HashMap<Stitch, AreaDrawer>()
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

    override fun draw(canvas: Canvas, sourceArea: Rect?, outputArea: Rect) {
        drawInternal(canvas, sourceArea ?: Rect(0, 0, overallWidth, overallHeight), outputArea);
    }

    private fun drawInternal(canvas: Canvas, sourceArea: Rect, outputArea: Rect) {
        val scale = outputArea.width() / sourceArea.width().toFloat()
        val padding = findSourcePadding(sourceArea)
        val paddedSourceArea = padding + sourceArea
        val paddedOutputArea = padding * scale + outputArea
        val firstRow = paddedSourceArea.top / (stitchSize + stitchPad)
        val lastRow = min(paddedSourceArea.bottom / (stitchSize + stitchPad), knitPattern.numRows - 1)
        val firstCol = paddedSourceArea.left / (stitchSize + stitchPad)
        var lastCol: Int
        val stitchDest = Rect(0, 0, (stitchSize * scale).toInt(), (stitchSize * scale).toInt())
        val saveCount = canvas.save()

        canvas.drawRect(paddedOutputArea, clearPaint)
        canvas.translate(paddedOutputArea.left, paddedOutputArea.top)
        canvas.translate(stitchPad * scale, stitchPad * scale)

        for (row in firstRow..lastRow) {
            lastCol = min(paddedSourceArea.right / (stitchSize + stitchPad) + 1, knitPattern.stitches[row].size - 1)
            canvas.save()

            for (col in firstCol..lastCol) {
                stitchDrawers[knitPattern.stitches[row][col]]?.draw(canvas, null, stitchDest)

                if(knitPattern.isStitchDone(row, col))
                    doneOverlayDrawer.draw(canvas, null, stitchDest)

                canvas.translate((stitchSize + stitchPad) * scale, 0F)
            }
            canvas.restore()
            canvas.translate(0F, (stitchSize + stitchPad) * scale)
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
