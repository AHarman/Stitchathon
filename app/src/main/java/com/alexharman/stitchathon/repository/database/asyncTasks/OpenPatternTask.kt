package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.os.AsyncTask
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

class OpenPatternTask(context: Context, listener: KnitPatternDataSource.OpenPatternListener) : AsyncTask<String, String, KnitPattern>() {
    private val context: WeakReference<Context> = WeakReference(context)
    private val listener: WeakReference<KnitPatternDataSource.OpenPatternListener> = WeakReference(listener)

    override fun doInBackground(vararg strings: String): KnitPattern? {
        val context = context.get() ?: return null
        return AppDatabase.getAppDatabase(context).knitPatternDao().getKnitPattern(strings[0], context)
    }

    override fun onPostExecute(knitPattern: KnitPattern?) {
        super.onPostExecute(knitPattern)
        val callback = listener.get() ?: return
        if (knitPattern == null) {
            callback.onOpenKnitPatternFail()
        } else {
            callback.onKnitPatternOpened(knitPattern)
        }
    }
}
