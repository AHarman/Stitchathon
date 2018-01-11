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
        return quantizeColours(sampledBitmap, numColours)
    }

    // TODO: selectively use colourReduce in countColours() method if too many colours
    private fun quantizeColours(bitmap: Bitmap, numColours: Int): Bitmap {
        val colourCount = countColours(bitmap)
        if (colourCount.keys.size <= numColours) return bitmap
        val distanceTable = createDistanceTable(colourCount.keys.toTypedArray())
        val colourMap = groupColours(colourCount, distanceTable, numColours)
        return replaceColours(bitmap, colourMap)
    }

    private fun replaceColours(bitmap: Bitmap, colourMap: HashMap<Int, Int>): Bitmap {
        for (row in 0 until bitmap.height) {
            for (col in 0 until bitmap.width) {
                bitmap.setPixel(col, row, colourMap[bitmap.getPixel(col, row)]!!)
            }
        }
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

    private fun groupColours(colourCount: HashMap<Colour, Int>,
                             distanceTable: HashMap<Colour, ArrayList<Colour>>,
                             coloursWanted: Int)
            : HashMap<Int, Int> {
        val groupedColours = ArrayList<ArrayList<Pair<Colour, Int>>>()

        //Building initial structure
        colourCount.entries.forEach({
            val al = ArrayList<Pair<Colour, Int>>()
            al.add(it.toPair())
            groupedColours.add(al)
        })
        groupedColours.sortBy { it.sumBy { it.second } }

        // Do an AV-style instant runoff election on colours.
        // Each colour is a candidate, has votes equal to the number of pixels it has in the image
        // Each colour's preference list is all the other colours in order of colour distance
        while (groupedColours.size > coloursWanted) {
            for ( pair in groupedColours.removeAt(0)) {
                distanceTable.values.forEach { it.remove(pair.first) }
                val movedTo = groupedColours.find { it[0].first == distanceTable[pair.first]!![0] }!!
                movedTo.add(pair)
                distanceTable[pair.first]!!.remove(movedTo[0].first)
            }
            groupedColours.sortBy { it.sumBy { it.second } }
        }

        val colourMapping = HashMap<Int, Int>()
        groupedColours.forEach { list -> list.forEach { colourMapping[it.first.toArgb()] = list[0].first.toArgb() } }
        return colourMapping
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

        fun toArgb(): Int = (0xFF shl 24) + (r shl 16) + (g shl 8) + b
    }
}
