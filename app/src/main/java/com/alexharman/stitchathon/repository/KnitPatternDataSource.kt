package com.alexharman.stitchathon.repository

import android.graphics.Bitmap
import android.net.Uri
import com.alexharman.stitchathon.KnitPackage.KnitPattern

interface KnitPatternDataSource {

    // Currently only using this to get thumbnails and names, but might expand to more info
    interface GetPatternInfoListener {

        fun onPatternInfoReturn(result: Array<Pair<String, Bitmap?>>)

        fun onGetKnitPatternInfoFail()
    }

    interface OpenPatternListener {
        fun onKnitPatternOpened(pattern: KnitPattern)

        fun onOpenKnitPatternFail()
    }

    interface CurrentPatternListener {
        fun onCurrentPatternChanged(patternName: String)
    }

    fun registerCurrentPatternListener(listener: CurrentPatternListener)

    fun deregisterCurrentPatternListener(listener: CurrentPatternListener)

    fun setCurrentKnitPattern(patternName: String)

    fun openKnitPattern(patternName: String, listener: OpenPatternListener?)

    fun getKnitPatternNames(callback: GetPatternInfoListener)

    fun getCurrentPatternName(): String?

    fun saveKnitPatternChanges(pattern: KnitPattern)

    // TODO: Move this to a service or something
    fun importNewJsonPattern(uri: Uri, callback: OpenPatternListener?)

    fun importNewBitmapPattern(uri: Uri,
                               name: String,
                               width: Int,
                               height: Int,
                               oddRowsOpposite: Boolean,
                               numColours: Int,
                               listener: OpenPatternListener?)

    fun deleteAllKnitPatterns()

    fun deleteKnitPatterns(vararg patternNames: String)
}