package com.alexharman.stitchathon.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
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

    private val currentPatternListeners = ArrayList<KnitPatternDataSource.CurrentPatternListener>()
    private val sharedPreferences: WeakReference<SharedPreferences> =
            WeakReference(PreferenceManager.getDefaultSharedPreferences(context))

    override fun registerCurrentPatternListener(listener: KnitPatternDataSource.CurrentPatternListener) {
        if (!currentPatternListeners.contains(listener))
            currentPatternListeners.add(listener)
    }

    override fun deregisterCurrentPatternListener(listener: KnitPatternDataSource.CurrentPatternListener) {
        if (currentPatternListeners.contains(listener))
            currentPatternListeners.remove(listener)
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

    override fun openKnitPattern(patternName: String, listener: KnitPatternDataSource.OpenPatternListener) {
        val context = context.get() ?: return listener.onOpenKnitPatternFail()
        OpenPatternTask(context, listener).execute(patternName)
    }

    override fun getKnitPatternNames(callback: KnitPatternDataSource.GetPatternInfoListener) {
        val context = context.get() ?: return callback.onGetKnitPatternInfoFail()
        GetNamesAndImagesTask(context, callback).execute()
    }

    override fun importNewJsonPattern(uri: String, listener: KnitPatternDataSource.ImportPatternListener?) {
        val context = context.get()
        when {
            context != null -> ImportJsonTask(context, listener).execute(uri)
            listener != null -> listener.onPatternImportFail()
            else -> return
        }
    }

    override fun importNewBitmapPattern(uri: String, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int, listener: KnitPatternDataSource.ImportPatternListener?) {
        val context = context.get()
        when {
            context != null -> ImportImageTask(context, listener, uri, name, width, height, oddRowsOpposite, numColours).execute()
            listener != null -> listener.onPatternImportFail()
            else -> return
        }
    }

    override fun deleteAllKnitPatterns() {
        val context = context.get() ?: return
        DeleteAllPatternsTask(context).execute()
    }

    override fun deleteKnitPatterns(vararg patternNames: String) {
        val context = context.get() ?: return
        DeletePatternsTask(context).execute(*patternNames)
        if (getCurrentPatternName() in patternNames) {
            val prefs = sharedPreferences.get() ?: return
            prefs
                    .edit()
                    .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                    .apply()
        }
    }
}