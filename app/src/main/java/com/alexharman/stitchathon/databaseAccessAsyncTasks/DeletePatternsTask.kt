package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.os.AsyncTask
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

class DeletePatternsTask(context: Context) : AsyncTask<String, Void, Void>() {
    val context = WeakReference<Context>(context)

    override fun doInBackground(vararg names: String): Void? {
        val dao = AppDatabase.Companion.getAppDatabase(context.get()!!).knitPatternDao()
        for (name in names) {
            dao.deletePattern(name, context.get()!!)
        }
        return null
    }
}
