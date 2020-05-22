package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.alexharman.stitchathon.importimage.ImageReader
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource

internal class ImportImageTask(context: Context,
                               callback: KnitPatternDataSource.OpenPatternListener?,
                               private val imageUri: Uri,
                               private val patternName: String,
                               private val width: Int,
                               private val height: Int,
                               private val oddRowsOpposite: Boolean,
                               private val numColours: Int) : ImportPatternTask<Void>(context, callback) {

    private lateinit var sourceImg: Bitmap

    override fun doInBackground(vararg voids: Void): KnitPattern {
        sourceImg = readImageFile(imageUri)!!
        val knitPattern = ImageReader().readImage(sourceImg, patternName, width, height, oddRowsOpposite, numColours)
        saveNewPattern(knitPattern)
        return knitPattern
    }

    private fun readImageFile(uri: Uri): Bitmap? {
        val context = context.get() ?: return null
        var bitmap: Bitmap? = null
        val opts = BitmapFactory.Options()
        opts.inMutable = true
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}