package com.alexharman.stitchathon.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.repository.KnitPatternDataSource.*
import com.alexharman.stitchathon.repository.database.AppDatabase
import com.alexharman.stitchathon.repository.database.KnitPatternDao
import com.alexharman.stitchathon.repository.database.asyncTasks.*
import java.lang.ref.WeakReference

class KnitPatternRepository private constructor(context: Context): KnitPatternDataSource {

    companion object {
        private var instance: KnitPatternRepository? = null
        private lateinit var dao: KnitPatternDao
        private lateinit var context: WeakReference<Context>

        fun getInstance(context: Context): KnitPatternRepository {
            val instance = this.instance ?: KnitPatternRepository(context)
            this.context = WeakReference(context)
            dao = AppDatabase.getAppDatabase(context.applicationContext).knitPatternDao()
            return instance
        }
    }

    private val currentPatternListeners = mutableSetOf<CurrentPatternListener>()
    private val importPatternListeners = mutableSetOf<ImportPatternListener>()
    private val sharedPreferences: WeakReference<SharedPreferences> =
            WeakReference(PreferenceManager.getDefaultSharedPreferences(context))

    override fun registerCurrentPatternListener(listener: CurrentPatternListener) {
        currentPatternListeners.add(listener)
    }

    override fun deregisterCurrentPatternListener(listener: CurrentPatternListener) {
        if (currentPatternListeners.contains(listener))
            currentPatternListeners.remove(listener)
    }

    override fun registerPatternImportedListener(listener: ImportPatternListener) {
         importPatternListeners.add(listener)
    }

    override fun deregisterPatternImportedListener(listener: ImportPatternListener) {
        if (importPatternListeners.contains(listener))
            importPatternListeners.remove(listener)
    }

    override fun setCurrentKnitPattern(patternName: String) {
        sharedPreferences
                .get()
                ?.edit()
                ?.putString(PreferenceKeys.CURRENT_PATTERN_NAME, patternName)
                ?.apply()
        currentPatternListeners.forEach { it.onCurrentPatternChanged(patternName) }
    }

    override fun getCurrentPatternName(): String? =
            sharedPreferences.get()?.getString(PreferenceKeys.CURRENT_PATTERN_NAME, null)

    override fun saveKnitPatternChanges(pattern: KnitPattern) {
        SavePatternChangesTask(dao).execute(pattern)
    }

    override fun openKnitPattern(patternName: String, listener: OpenPatternListener) {
        val context = context.get() ?: return listener.onOpenKnitPatternFail()
        OpenPatternTask(context, listener).execute(patternName)
    }

    override fun getKnitPatternsInfo(callback: GetPatternInfoListener) {
        val context = context.get() ?: return callback.onGetKnitPatternInfoFail()
        GetNamesAndImagesTask(context, callback).execute()
    }

    override fun importNewJsonPattern(
            uri: String,
            name: String,
            oddRowsOpposite: Boolean,
            listener: ImportPatternListener?) {

        val listeners =
                if (listener != null)
                    setOf(*importPatternListeners.toTypedArray(), listener)
                else
                    setOf(*importPatternListeners.toTypedArray())

        val context = context.get() ?: return listeners.forEach { it.onPatternImportFail() }
        ImportJsonTask(context, listeners, uri, name, oddRowsOpposite).execute()
    }

    override fun importNewBitmapPattern(
            uri: String,
            name: String,
            width: Int,
            height: Int,
            oddRowsOpposite: Boolean,
            numColours: Int,
            listener: ImportPatternListener?) {

        val listeners =
                if (listener != null)
                    setOf(*importPatternListeners.toTypedArray(), listener)
                else
                    setOf(*importPatternListeners.toTypedArray())

        val context = Companion.context.get() ?: return listeners.forEach { it.onPatternImportFail() }

        ImportImageTask(context, listeners, uri, name, width, height, oddRowsOpposite, numColours).execute()
    }

    override fun deleteAllKnitPatterns() {
        val context = context.get() ?: return
        DeleteAllPatternsTask(context).execute()
        val prefs = sharedPreferences.get() ?: return
        prefs
                .edit()
                .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                .apply()
    }

    override fun deleteKnitPatterns(patternNames: Collection<String>) {
        val context = context.get() ?: return
        DeletePatternsTask(context).execute(*patternNames.toTypedArray())
        if (getCurrentPatternName() in patternNames) {
            val prefs = sharedPreferences.get() ?: return
            prefs
                    .edit()
                    .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                    .apply()
        }
    }

    override fun clearPreferences() {
        sharedPreferences
                .get()
                ?.edit()
                ?.clear()
                ?.apply()
        PreferenceManager.setDefaultValues(context.get(), R.xml.preferences, true)
    }

    override fun getPatternPreferences(patternName: String): KnitPatternPreferences {
        val prefs = sharedPreferences.get()!!
        val context = context.get()!!
        return KnitPatternPreferences(
                backgroundColor = prefs.getInt(PreferenceKeys.BACKGROUND_COLOUR, R.color.default_pattern_background_colour),
                stitchColors = arrayOf(
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_1, context.getColor(R.color.default_stitch_colour_1)),
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_2, context.getColor(R.color.default_stitch_colour_2)),
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_3, context.getColor(R.color.default_stitch_colour_3))
                ),
                stitchPad = prefs.getInt(PreferenceKeys.STITCH_PAD, 1),
                stitchSize = prefs.getInt(PreferenceKeys.STITCH_SIZE, 1)
        )
    }
}