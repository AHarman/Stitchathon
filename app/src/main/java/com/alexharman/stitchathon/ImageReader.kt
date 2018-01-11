package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.util.Log
import kotlin.math.sqrt

/**
 * Created by Alex on 06/01/2018.
 *
 * Reduces image size and reduces colours to get a neat pattern from an image
 *
 * Colour quantizing is meant for jpeg artifacting and similar, this slows to a crawl on > 250 colours
 */

class ImageReader {
    fun readImage(bitmap: Bitmap, stitchesWide: Int, stitchesHigh: Int, numColours: Int): Bitmap {
        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, stitchesWide, stitchesHigh, false)
        quantizeColours(sampledBitmap, numColours)
        return sampledBitmap
    }

    // TODO: selectively use colourReduce in countColours() method if too many colours
    private fun quantizeColours(bitmap: Bitmap, numColours: Int): Bitmap {
        val colourCount = countColours(bitmap)
        if (colourCount.keys.size <= numColours) return bitmap
        Log.d("quantizing", "We have ${colourCount.keys.size} colours in total")
        val distanceTable = createDistanceTable(colourCount.keys.toTypedArray())

        // Do more processing here if needed
        Log.d("quantizing", "We have ${colourCount.keys.size} colours")
        Log.d("quantizing", "We want to reduce that to $numColours")
        return bitmap
    }

    private fun countColours(bitmap: Bitmap, colourReduce: Int = 0): HashMap<Colour, Int> {
        val colourCount: HashMap<Colour, Int> = HashMap()
        var currentPixel: Colour
        var currentPixelCount: Int

        for (row in 0 until bitmap.height) {
            for (col in 0 until bitmap.width) {
                currentPixel = Colour(bitmap.getPixel(col, row), colourReduce)
                currentPixelCount = colourCount[currentPixel] ?: 0
                colourCount.put(currentPixel, currentPixelCount + 1)
            }
        }
        return colourCount
    }

    private fun createDistanceTable(colours: Array<Colour>): HashMap<Colour, ArrayList<Colour>> {
        val distanceTable = HashMap<Colour, ArrayList<Colour>>()
        var row: ArrayList<Colour>
        for (colour in colours) {
            row = ArrayList()
            row.addAll(colours.filter { it != colour }.sortedBy { colourDistance(it, colour) } )
            distanceTable[colour] = row
        }
        return distanceTable
    }

    private fun colourDistance(c1: Colour, c2:Colour): Double {
        val rDist = c1.r - c2.r
        val gDist = c1.g - c2.g
        val bDist = c1.b - c2.b
        return sqrt((rDist*rDist + gDist*gDist + bDist*bDist).toDouble())
    }

    private data class Colour(val r: Int, val g: Int, val b: Int) {
        constructor(argb: Int) : this(argb, 0)

        constructor(argb: Int, colourReduce: Int) : this(
                (argb and 0x00FF0000) shr (16 + colourReduce),
                (argb and 0x0000FF00) shr (8 + colourReduce),
                (argb and 0x000000FF) shr colourReduce)
    }
}
