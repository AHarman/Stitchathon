package com.alexharman.stitchathon

import android.graphics.Bitmap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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
        val colours = bitmapToColours(bitmap)
        if (colours.distinct().size <= numColours) return bitmap
        val colourMap = groupColours(colours, numColours)
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

    private fun bitmapToColours(bitmap: Bitmap): ArrayList<Colour> {
        val arr = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(arr, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return arr.map { Colour(it) } as ArrayList<Colour>
    }

    // Use k-means++
    private fun groupColours(pixels: ArrayList<Colour>, coloursWanted: Int): HashMap<Int, Int> {
        val centroids = pickInitialCentroids(pixels, coloursWanted)
        var groups = HashMap<Colour, ArrayList<Colour>>()
        var changesMade = true
        centroids.forEach { groups[it] = ArrayList()}

        while (changesMade) {
            changesMade = false
            // Assignment step
            for (pixel in pixels) {
                groups[centroids.minBy { sqColourDistance(it, pixel) }]?.add(pixel)
            }

            // Update step
            val newGroups = HashMap<Colour, ArrayList<Colour>>()
            for ((centroid, group) in groups) {
                val newCentroid = Colour(
                        group.sumBy { it.r } / group.size,
                        group.sumBy { it.g } / group.size,
                        group.sumBy { it.b } / group.size)

                newGroups[newCentroid] = group
                if (newCentroid != centroid) {
                    changesMade = true
                    centroids.remove(centroid)
                    centroids.add(newCentroid)
                }
            }
            groups = newGroups
        }

        val colourMap = HashMap<Int, Int>()
        for ( (centroid, group) in groups) {
            group.distinct().forEach { colourMap[it.toArgb()] = centroid.toArgb() }
        }
        return colourMap
    }

    private fun pickInitialCentroids(pixels: ArrayList<Colour>, numCentroids: Int): ArrayList<Colour> {
        val rng = Random()
        var distanceToCentroids: HashMap<Colour, Int>
        val centroids = ArrayList<Colour>()
        val initRandom = rng.nextInt(pixels.size)
        centroids.add(pixels[initRandom])

        while (centroids.size < numCentroids) {
            distanceToCentroids = findDistanceToCentroid(centroids, pixels)

            var randNum = rng.nextInt(distanceToCentroids.values.sum())
            for( (colour, dist) in distanceToCentroids) {
                randNum -= dist
                if (randNum <= 0) {
                    centroids.add(colour)
                    break
                }
            }
        }
        return centroids
    }

    private fun findDistanceToCentroid(centroids: ArrayList<Colour>,
                                       pixels: ArrayList<Colour>): HashMap<Colour, Int> =
        pixels.associate{ pixel -> Pair( pixel, centroids.map{sqColourDistance(it, pixel) }.min()!! ) } as HashMap<Colour, Int>

    private fun sqColourDistance(c1: Colour, c2: Colour): Int {
        val rDist = c1.r - c2.r
        val gDist = c1.g - c2.g
        val bDist = c1.b - c2.b
        return rDist*rDist + gDist*gDist + bDist*bDist
    }

    private fun colourDistance(c1: Colour, c2:Colour): Double =
            sqrt(sqColourDistance(c1, c2).toDouble())

    private data class Colour(val r: Int, val g: Int, val b: Int) {
        constructor(argb: Int) : this(argb, 0)

        constructor(argb: Int, colourReduce: Int) : this(
                (argb and 0x00FF0000) shr (16 + colourReduce),
                (argb and 0x0000FF00) shr (8 + colourReduce),
                (argb and 0x000000FF) shr colourReduce)

        fun toArgb(): Int = (0xFF shl 24) + (r shl 16) + (g shl 8) + b
    }
}
