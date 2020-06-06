package com.alexharman.stitchathon.repository

import android.graphics.Bitmap
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences

interface KnitPatternDataSource {

    // TODO: Stop using asynctask and see if we can avoid these interfaces
    // Currently only using this to get thumbnails and names, but might expand to more info
    interface GetPatternInfoListener {
        fun onPatternInfoReturn(result: Array<Pair<String, Bitmap?>>)

        fun onGetKnitPatternInfoFail()
    }

    interface OpenPatternListener {
        fun onKnitPatternOpened(pattern: KnitPattern)

        fun onOpenKnitPatternFail()
    }

    interface ImportPatternListener {
        fun onPatternImport(pattern: KnitPattern)

        fun onPatternImportFail()
    }

    interface CurrentPatternListener {
        fun onCurrentPatternChanged(patternName: String)
    }

    fun registerCurrentPatternListener(listener: CurrentPatternListener)

    fun deregisterCurrentPatternListener(listener: CurrentPatternListener)

    fun setCurrentKnitPattern(patternName: String)

    fun openKnitPattern(patternName: String, listener: OpenPatternListener)

    fun getKnitPatternNames(callback: GetPatternInfoListener)

    fun getCurrentPatternName(): String?

    fun saveKnitPatternChanges(pattern: KnitPattern)

    // TODO: Move this to a service or something, have "save new pattern" as a repo method
    fun importNewJsonPattern(
            uri: String,
            name: String,
            oddRowsOpposite: Boolean,
            listener: ImportPatternListener? = null
    )

    fun importNewBitmapPattern(
            uri: String,
            name: String,
            width: Int,
            height: Int,
            oddRowsOpposite: Boolean,
            numColours: Int,
            listener: ImportPatternListener? = null
    )

    fun deleteAllKnitPatterns()

    fun deleteKnitPatterns(vararg patternNames: String)

    fun getPatternPreferences(patternName: String): KnitPatternPreferences

    fun clearPreferences()
}