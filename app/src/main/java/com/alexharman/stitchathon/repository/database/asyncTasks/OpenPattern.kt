package com.alexharman.stitchathon.repository.database.asyncTasks

import android.graphics.Bitmap
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer

interface OpenPattern {
    fun onPatternReturned(knitPattern: KnitPattern, knitPatternDrawer: KnitPatternDrawer, thumbnail: Bitmap)
}
