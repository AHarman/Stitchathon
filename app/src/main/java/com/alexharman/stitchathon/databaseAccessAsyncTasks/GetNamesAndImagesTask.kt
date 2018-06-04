package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

class GetNamesAndImagesTask(context: Context, callback: GetNamesAndThumbnails) : AsyncTask<Void, Void, Array<Pair<String, Bitmap>>>() {
    private val context = WeakReference(context)
    private val callback = WeakReference(callback)

    override fun doInBackground(vararg voids: Void): Array<Pair<String, Bitmap>> {
        return AppDatabase.getAppDatabase(context.get()!!).knitPatternDao().getThumbnails(context.get()!!)
    }

    override fun onPostExecute(result: Array<Pair<String, Bitmap>>) {
        callback.get()!!.onNamesAndThumbnailsReturn(result)
    }

    interface GetNamesAndThumbnails {
        fun onNamesAndThumbnailsReturn(result: Array<Pair<String, Bitmap>>)
    }
}
