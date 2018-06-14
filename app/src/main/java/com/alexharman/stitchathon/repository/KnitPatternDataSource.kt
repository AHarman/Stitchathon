package com.alexharman.stitchathon.repository

import android.graphics.Bitmap
import android.net.Uri
import com.alexharman.stitchathon.KnitPackage.KnitPattern

interface KnitPatternDataSource {

    // Currently only using this to get thumbnails and names, but might expand to more info
    interface GetPatternInfoCallback {

        fun onPatternInfoReturn(result: Array<Pair<String, Bitmap?>>)

        fun onGetKnitPatternInfoFail()
    }

    interface OpenKnitPatternCallback {

        fun onKnitPatternOpened(pattern: KnitPattern)

        fun onOpenKnitPatternFail()
    }

    fun openKnitPattern(patternName: String, callback: OpenKnitPatternCallback)

    fun getKnitPatternNames(callback: GetPatternInfoCallback)

    fun saveKnitPatternChanges(pattern: KnitPattern)

    fun importNewPattern(uri: Uri)

    fun deleteAllKnitPatterns()

    fun deleteKnitPatterns(vararg patternNames: String)
}