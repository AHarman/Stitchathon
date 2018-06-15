package com.alexharman.stitchathon.repository

import android.content.Context
import android.net.Uri
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.database.AppDatabase
import com.alexharman.stitchathon.repository.database.KnitPatternDao
import com.alexharman.stitchathon.repository.database.asyncTasks.*
import java.lang.ref.WeakReference

class KnitPatternRepository private constructor(): KnitPatternDataSource {

    companion object {
        private var instance: KnitPatternRepository? = null
        private lateinit var dao: KnitPatternDao
        private lateinit var context: WeakReference<Context>

        fun getInstance(context: Context): KnitPatternRepository {
            val newInstance = instance ?: KnitPatternRepository()
            instance = newInstance
            dao = AppDatabase.getAppDatabase(context.applicationContext).knitPatternDao()
            this.context = WeakReference(context.applicationContext)

            return newInstance
        }
    }

    override fun openKnitPattern(patternName: String, callback: KnitPatternDataSource.OpenKnitPatternCallback) {
        val context = context.get() ?: return callback.onOpenKnitPatternFail()
        OpenPatternTask(context, callback).execute(patternName)
    }

    override fun getKnitPatternNames(callback: KnitPatternDataSource.GetPatternInfoCallback) {
        val context = context.get() ?: return callback.onGetKnitPatternInfoFail()
        GetNamesAndImagesTask(context, callback).execute()
    }

    override fun saveKnitPatternChanges(pattern: KnitPattern) {
        SavePatternChangesTask(dao).execute(pattern)
    }

    override fun importNewJsonPattern(uri: Uri, callback: KnitPatternDataSource.OpenKnitPatternCallback?) {
        val context = context.get()
        when {
            context != null -> ImportJsonTask(context, callback).execute(uri)
            callback != null -> callback.onOpenKnitPatternFail()
            else -> return
        }
    }

    override fun importNewBitmapPattern(uri: Uri, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int, callback: KnitPatternDataSource.OpenKnitPatternCallback?) {
        val context = context.get()
        when {
            context != null -> ImportImageTask(context, callback, uri, name, width, height, oddRowsOpposite, numColours).execute()
            callback != null -> callback.onOpenKnitPatternFail()
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
    }
}