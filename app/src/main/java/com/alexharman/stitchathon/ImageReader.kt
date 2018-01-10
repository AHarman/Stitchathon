package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.sqrt

/**
 * Created by Alex on 06/01/2018.
 */

class ImageReader {
    fun readImage(bitmap: Bitmap, stitchesWide: Int, stitchesHigh: Int, numColours: Int): Bitmap {
        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, stitchesWide, stitchesHigh, true)
        quantizeColours(sampledBitmap, numColours)
        return sampledBitmap
    }

    private fun quantizeColours(bitmap: Bitmap, numColours: Int): Bitmap {
        val colourCount = countColours(bitmap)
        if (colourCount.keys.size <= numColours) {
            return bitmap
        }

        // Do more processing here if needed
        Log.d("quantizing", "We have ${colourCount.keys.size} colours")
        Log.d("quantizing", "We want to reduce that to $numColours")
        return bitmap
    }

    private fun countColours(bitmap: Bitmap): HashMap<Colour, Int> {
        val colourCount: HashMap<Colour, Int> = HashMap()
        var currentPixel: Colour
        var currentPixelCount: Int

        for (row in 0 until bitmap.height) {
            for (col in 0 until bitmap.width) {
                currentPixel = Colour(bitmap.getPixel(col, row))
                currentPixelCount = colourCount[currentPixel] ?: 0
                colourCount.put(currentPixel, currentPixelCount + 1)
            }
        }
        return colourCount
    }

    private data class Colour(val r: Int, val g: Int, val b: Int) {
        constructor(argb: Int) : this(
                (argb and 0x00FF0000) shr 16,
                (argb and 0x0000FF00) shr 8,
                argb and 0x000000FF)
    }
}
