package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.graphics.Bitmap
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer

interface OpenPattern {
    fun onPatternReturned(knitPattern: KnitPattern, knitPatternDrawer: KnitPatternDrawer, thumbnail: Bitmap)
}
