package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.os.AsyncTask
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

class DeleteAllPatternsTask(context: Context) : AsyncTask<Void, Void, Void>() {
    val context: WeakReference<Context> = WeakReference(context)
    override fun doInBackground(vararg params: Void): Void? {
        val context = context.get() ?: return null
        AppDatabase.getAppDatabase(context).knitPatternDao().deleteAllPatterns(context)
        return null
    }
}
