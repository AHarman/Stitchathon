package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by Alex on 06/01/2018.
 */

class ImageReader {
    fun readImage(bitmap: Bitmap, stitchesWide: Int, stitchesHigh: Int, numColours: Int): Bitmap {
        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, stitchesWide, stitchesHigh, true);
        return sampledBitmap
    }
}