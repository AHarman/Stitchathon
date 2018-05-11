package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer
import com.alexharman.stitchathon.ProgressbarDialog
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

class OpenPatternTask(context: AppCompatActivity, callback: OpenPattern) : AsyncTask<String, String, KnitPattern>() {
    private var progressbarDialog = ProgressbarDialog.newInstance(context.getString(R.string.progress_dialog_load_title), true, context.getString(R.string.progress_bar_loading_pattern))
    private lateinit var knitPatternDrawer: KnitPatternDrawer
    private var thumbnail: Bitmap? = null
    private val context: WeakReference<AppCompatActivity> = WeakReference(context)
    private val callback: WeakReference<OpenPattern> = WeakReference(callback)

    override fun onPreExecute() {
        progressbarDialog.show(context.get()!!.supportFragmentManager, "Opening")
    }

    override fun doInBackground(vararg strings: String): KnitPattern {
        val dao = AppDatabase.getAppDatabase(context.get()!!).knitPatternDao()
        val knitPattern = dao.getKnitPattern(strings[0], context.get()!!)
        thumbnail = dao.getThumbnail(context.get()!!, knitPattern.name)
        publishProgress(context.get()!!.getString(R.string.progress_bar_creating_bitmap))
        knitPatternDrawer = KnitPatternDrawer(knitPattern, context.get()!!)
        return knitPattern
    }

    override fun onProgressUpdate(vararg values: String) {
        progressbarDialog.updateText(values[0])
    }

    override fun onPostExecute(knitPattern: KnitPattern) {
        super.onPostExecute(knitPattern)
        callback.get()!!.onPatternReturned(knitPattern, knitPatternDrawer/*, thumbnail*/)
        progressbarDialog.dismiss()
    }
}
