package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.alexharman.stitchathon.ImageReader
import com.alexharman.stitchathon.KnitPackage.KnitPattern

internal class ImportImageTask(context: AppCompatActivity,
                               callback: OpenPattern,
                               private val imageUri: Uri,
                               private val patternName: String,
                               private val width: Int,
                               private val height: Int,
                               private val numColours: Int) : ImportPatternTask<Void>(context, callback) {

    private lateinit var sourceImg: Bitmap

    override fun doInBackground(vararg voids: Void): KnitPattern {
        sourceImg = readImageFile(imageUri)!!
        val knitPattern = ImageReader().readImage(sourceImg, patternName, width, height, numColours)
        saveNewPattern(knitPattern)
        return knitPattern
    }

    private fun readImageFile(uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        val opts = BitmapFactory.Options()
        opts.inMutable = true
        try {
            val inputStream = context.get()!!.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}