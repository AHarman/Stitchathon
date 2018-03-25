package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.os.AsyncTask
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

class DeleteAllPatternsTask(context: Context) : AsyncTask<Void, Void, Void>() {
    var context: WeakReference<Context> = WeakReference(context)
    override fun doInBackground(vararg params: Void): Void? {
        AppDatabase.getAppDatabase(context.get()!!).knitPatternDao().deleteAllPatterns(context.get()!!)
        return null
    }
}
