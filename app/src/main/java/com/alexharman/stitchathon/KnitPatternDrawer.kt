package com.alexharman.stitchathon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.util.Log
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class KnitPatternDrawer(val knitPattern: KnitPattern, context: Context) {
    private val stitchSize: Float
    private val stitchPad: Float
    private val colours: IntArray
    private var stitchBitmaps: HashMap<Stitch, Bitmap>
    private var stitchPaints: HashMap<Stitch, Paint>
    private var doneOverlayPaint: Paint
    lateinit var patternBitmap: Bitmap
        private set

    private var currentView = RectF(0f, 0f, 0f, 0f)
    private var lockToScreen: Boolean
    private var fitPatternWidth: Boolean

    private val preferenceListener = MySharedPreferenceListener(context)
    private val undoStack = Stack<Int>()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)

        colours = IntArray(3)
        colours[0] = prefs.getInt(context.getString(R.string.app_options_stitch_colour_key_1), -1)
        colours[1] = prefs.getInt(context.getString(R.string.app_options_stitch_colour_key_2), -1)
        colours[2] = prefs.getInt(context.getString(R.string.app_options_stitch_colour_key_3), -1)
        stitchSize = prefs.getString(context.getString(R.string.app_options_stitch_size_key), "").toFloat()
        stitchPad = prefs.getString(context.getString(R.string.app_options_stitch_pad_key), "").toFloat()
        lockToScreen = prefs.getBoolean(context.getString(R.string.lock_to_screen_key), false)
        fitPatternWidth = prefs.getBoolean(context.getString(R.string.fit_pattern_width_key), false)

        stitchPaints = createStitchPaints(knitPattern.stitchTypes)
        doneOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        doneOverlayPaint.style = Paint.Style.FILL
        doneOverlayPaint.colorFilter = LightingColorFilter(0x00FFFFFF, 0x00888888)
        stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes)
    }

    fun createPatternBitmap(width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)

        currentView = RectF(0f, 0f, min(width.toFloat(), knitPattern.patternWidth * (stitchPad + stitchSize) + stitchPad), min(height.toFloat(), knitPattern.stitches.size * (stitchPad + stitchSize) + stitchPad))
        patternBitmap = bitmap
        scrollToNextStitch()
        drawPattern()
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

    fun drawPattern() {
        val canvas = Canvas(patternBitmap)
        // Something in the next 4 lines is wrong.
        var col = floor(currentView.left / (stitchSize + stitchPad)).toInt()
        var row = floor(currentView.top / (stitchSize + stitchPad)).toInt()
        var totalXTranslate = max(currentView.left, stitchPad)
        var totalYTranslate = max(currentView.top, stitchPad)
        canvas.translate(totalXTranslate, totalYTranslate)
//        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)

        Log.d("Drawing pattern", "currentView top: ${currentView.top}, left: ${currentView.left}")
        Log.d("Drawing pattern", "currentView bottom: ${currentView.bottom}, right: ${currentView.right}")

        while (totalYTranslate < currentView.bottom) {
            canvas.save()
            while (totalXTranslate < currentView.right) {
                Log.d("Drawing pattern", "row: $row, col: $col")
                Log.d("Drawing pattern", "x: $totalXTranslate, y: $totalYTranslate")
                val isDone = row < knitPattern.currentRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == 1 && col < knitPattern.nextStitchInRow ||
                        row == knitPattern.currentRow && knitPattern.rowDirection == -1 && col > knitPattern.nextStitchInRow
                drawStitch(canvas, knitPattern.stitches[row][col], isDone)
                totalXTranslate += (stitchPad + stitchSize) * knitPattern.stitches[row][col].width
                canvas.translate(stitchPad + stitchSize, 0f)
                col++
            }
            canvas.restore()
            totalXTranslate = max(currentView.left - stitchSize, stitchPad)
            col = floor(currentView.top / (stitchSize + stitchPad)).toInt()

            row++
            totalYTranslate += stitchPad + stitchSize
            canvas.translate(0f, stitchPad + stitchSize)
        }
    }

    private fun drawStitch(canvas: Canvas, stitch: Stitch, isDone: Boolean) {
        val b = stitchBitmaps[stitch]
        canvas.drawBitmap(b, 0f, 0f, if (isDone) doneOverlayPaint else null)
    }

    fun undo() {
//        if (undoStack.size == 0) {
//            undoRow()
//            return
//        }
//        for (i in 0 until undoStack.pop()) {
//            knitPattern.undoStitch()
//            drawNextStitch(false)
//        }
    }

    private fun undoRow() {
//        do {
//            knitPattern.undoStitch()
//            drawNextStitch(false)
//        } while (!knitPattern.isStartOfRow)
    }

    fun increment(numStitches: Int = 1) {
//        undoStack.push(numStitches)
//        for (i in 0 until numStitches) {
//            drawNextStitch(true)
//            knitPattern.increment()
//        }
//        if(lockToScreen) scrollToNextStitch()
    }

    fun incrementRow() {
//        increment(knitPattern.stitchesLeftInRow)
    }

    fun markStitchesTo(row: Int, col: Int) {
//        undoStack.clear()
//        while (row > knitPattern.currentRow) {
//            incrementRow()
//        }
//        while (row < knitPattern.currentRow) {
//            undoRow()
//        }
//        while (col > knitPattern.stitchesDoneInRow) {
//            increment()
//        }
//        while (col < knitPattern.stitchesDoneInRow) {
//            knitPattern.undoStitch()
//            drawNextStitch(false)
//        }
//        undoStack.clear()
    }

//    private fun positionOfNextStitch(): Pair<Float, Float> {
//        val x = knitPattern.currentDistanceInRow * (stitchSize + stitchPad) * knitPattern.rowDirection +
//                if (knitPattern.rowDirection == -1) patternBitmap.width else 0
//        val y = knitPattern.currentRow * (stitchSize + stitchPad)
//        return Pair(x, y)
//    }

    fun incrementBlock() {
//        val stitchType = knitPattern.stitches[knitPattern.currentRow][knitPattern.nextStitchInRow].type
//        val currentRow = knitPattern.stitches[knitPattern.currentRow]
//        var stitchesToDo = 0
//        while (stitchesToDo + knitPattern.stitchesDoneInRow < currentRow.size &&
//                currentRow[knitPattern.nextStitchInRow + stitchesToDo * knitPattern.rowDirection].type == stitchType) {
//            stitchesToDo++
//        }
//        increment(stitchesToDo)
    }

    // TODO: Enable and fix these

    private fun scrollToNextStitch() {
//        val (newCentreX, newCentreY) = positionOfNextStitch()
//        val centreX = currentView.centerX()
//        val centreY = currentView.centerY()
//        moveCurrentViewAndCheckBounds((newCentreX - centreX).toInt(), (newCentreY - centreY).toInt())
    }

    internal fun scroll(distanceX: Float, distanceY: Float) {
//        if (lockToScreen) return
//        val ratio = currentView.width() / patternBitmap.width
//
//        moveCurrentViewAndCheckBounds((distanceX * ratio).toInt(), (distanceY * ratio).toInt())
//        drawPattern()
    }


    private fun zoomPattern() {
//        this.fitPatternWidth = fitPatternWidth
//        zoomSrcRect()
//        drawPattern()
    }

    private fun zoomSrcRect() {
//        val centreX = currentView.centerX()
//        val centreY = currentView.centerY()
//        val srcRectHeight: Int
//        val srcRectWidth: Int
//
//        if (fitPatternWidth) {
//            val ratio = height.toFloat() / width.toFloat()
//            srcRectHeight = min((ratio * patternBitmap.width).toInt(), patternBitmap.height)
//            currentView.left = 0
//            currentView.right = patternBitmap.width
//        } else {
//            srcRectWidth = min(width, patternBitmap.width)
//            srcRectHeight = min(height, patternBitmap.height)
//            currentView.left = centreX - srcRectWidth / 2
//            currentView.right = centreX + srcRectWidth / 2
//        }
//
//        currentView.top = centreY - srcRectHeight / 2
//        currentView.bottom = centreY + srcRectHeight / 2
//        moveCurrentViewAndCheckBounds(0, 0)
    }

    private fun moveCurrentViewAndCheckBounds(shiftX: Int, shiftY: Int) {
//        var myShiftX = shiftX.toFloat()
//        var myShiftY = shiftY.toFloat()
//
//        if (fitPatternWidth) {
//            myShiftX = 0f
//        } else {
//            if (currentView.left + shiftX < 0) {
//                myShiftX = -currentView.left
//            } else if (currentView.right + shiftX > patternBitmap.width) {
//                myShiftX = patternBitmap.width - currentView.right
//            }
//        }
//
//        if (currentView.top + shiftY < 0) {
//            myShiftY = -currentView.top
//        } else if (currentView.bottom + shiftY > patternBitmap.height) {
//            myShiftY = patternBitmap.height - currentView.bottom
//        }
//
//        currentView.left += myShiftX
//        currentView.right += myShiftX
//        currentView.top += myShiftY
//        currentView.bottom += myShiftY
    }

    fun resize(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class MySharedPreferenceListener(val context: Context): SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == context.getString(R.string.lock_to_screen_key)) {
                lockToScreen = sharedPreferences.getBoolean(key, lockToScreen)
                if (lockToScreen) scrollToNextStitch()
                drawPattern()
            }
            if (key == context.getString(R.string.fit_pattern_width_key)) {
                fitPatternWidth = sharedPreferences.getBoolean(key, false)
                zoomPattern()
            }
        }
    }
}
