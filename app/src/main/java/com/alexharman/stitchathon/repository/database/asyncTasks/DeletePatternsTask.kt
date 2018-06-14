package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.os.AsyncTask
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

class DeletePatternsTask(context: Context) : AsyncTask<String, Void, Void>() {
    val context = WeakReference<Context>(context)

    override fun doInBackground(vararg names: String): Void? {
        val context = context.get() ?: return null
        AppDatabase.getAppDatabase(context).knitPatternDao().deletePatterns(*names, context = context)
        return null
    }
}
