package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference
import java.util.*

class GetNamesAndImagesTask(context: Context, callback: GetNamesAndThumbnails) : AsyncTask<Void, Void, HashMap<String, Bitmap?>>() {
    private val context = WeakReference(context)
    private val callback = WeakReference(callback)

    override fun doInBackground(vararg voids: Void): HashMap<String, Bitmap?> {
        return AppDatabase.getAppDatabase(context.get()!!).knitPatternDao().getThumbnails(context.get()!!)
    }

    override fun onPostExecute(map: HashMap<String, Bitmap?>) {
        callback.get()!!.onNamesAndThumbnailsReturn(map)
    }

    interface GetNamesAndThumbnails {
        fun onNamesAndThumbnailsReturn(map: HashMap<String, Bitmap?>)
    }
}
