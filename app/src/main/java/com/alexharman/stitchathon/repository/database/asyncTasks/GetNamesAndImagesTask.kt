package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

class GetNamesAndImagesTask(context: Context, callback: KnitPatternDataSource.GetPatternInfoListener) : AsyncTask<Void, Void, Array<Pair<String, Bitmap?>>>() {
    private val context = WeakReference(context)
    private val callback = WeakReference(callback)

    override fun doInBackground(vararg voids: Void): Array<Pair<String, Bitmap?>>? {
        val context = context.get() ?: return null
        return AppDatabase.getAppDatabase(context).knitPatternDao().getThumbnails(context)
    }

    override fun onPostExecute(result: Array<Pair<String, Bitmap?>>?) {
        if (result == null) {
            callback.get()?.onGetKnitPatternInfoFail()
        } else {
            callback.get()?.onPatternInfoReturn(result)
        }
    }

}
