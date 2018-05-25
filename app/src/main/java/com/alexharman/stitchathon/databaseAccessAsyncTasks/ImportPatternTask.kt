package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer
import com.alexharman.stitchathon.ProgressbarDialog
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

internal abstract class ImportPatternTask<V> internal constructor(context: AppCompatActivity, callback: OpenPattern) : AsyncTask<V, String, KnitPattern>() {
    private var progressbarDialog = ProgressbarDialog.newInstance(context.getString(R.string.progress_dialog_import_title), true, context.getString(R.string.progress_bar_importing_pattern))
    protected var context: WeakReference<AppCompatActivity> = WeakReference(context)
    private var callBack: WeakReference<OpenPattern> = WeakReference(callback)
    private lateinit var knitPatternDrawer: KnitPatternDrawer
    private lateinit var thumbnail: Bitmap

    override fun onPreExecute() {
        progressbarDialog.show(context.get()!!.supportFragmentManager, "Importing image")
    }

    internal fun saveNewPattern(knitPattern: KnitPattern) {
        publishProgress(context.get()!!.getString(R.string.progress_bar_creating_bitmap))
        knitPatternDrawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context.get()))
        thumbnail = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)
        publishProgress(context.get()!!.getString(R.string.progress_bar_saving_pattern))
        AppDatabase.getAppDatabase(context.get()!!).knitPatternDao().saveNewPattern(knitPattern, thumbnail, context.get()!!)
    }

    override fun onProgressUpdate(vararg values: String) {
        progressbarDialog.updateText(values[0])
    }

    override fun onPostExecute(pattern: KnitPattern?) {
        super.onPostExecute(pattern)
        if (pattern != null) {
            callBack.get()!!.onPatternReturned(pattern, knitPatternDrawer, thumbnail)
        }
        progressbarDialog.dismiss()
    }
}
