package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

class OpenPatternTask(context: Context, callback: OpenPattern) : AsyncTask<String, String, KnitPattern>() {
    private lateinit var knitPatternDrawer: KnitPatternDrawer
    private lateinit var thumbnail: Bitmap
    private val context: WeakReference<Context> = WeakReference(context)
    private val callback: WeakReference<OpenPattern> = WeakReference(callback)

    override fun doInBackground(vararg strings: String): KnitPattern {
        val dao = AppDatabase.getAppDatabase(context.get()!!).knitPatternDao()
        val knitPattern = dao.getKnitPattern(strings[0], context.get()!!)
        thumbnail = dao.getThumbnail(context.get()!!, knitPattern.name)
        publishProgress(context.get()!!.getString(R.string.progress_bar_creating_bitmap))
        knitPatternDrawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context.get()))
        return knitPattern
    }

    override fun onPostExecute(knitPattern: KnitPattern) {
        super.onPostExecute(knitPattern)
        callback.get()!!.onPatternReturned(knitPattern, knitPatternDrawer, thumbnail)
    }
}
