package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

internal abstract class ImportPatternTask<V> internal constructor(context: Context, callback: KnitPatternDataSource.OpenKnitPatternCallback)
    : AsyncTask<V, String, KnitPattern>() {
    protected var context: WeakReference<Context> = WeakReference(context)
    private var callback: WeakReference<KnitPatternDataSource.OpenKnitPatternCallback> = WeakReference(callback)
    private lateinit var knitPatternDrawer: KnitPatternDrawer
    private lateinit var thumbnail: Bitmap

    internal fun saveNewPattern(knitPattern: KnitPattern) {
        val context = context.get() ?: return
        knitPatternDrawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context))
        thumbnail = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)
        AppDatabase.getAppDatabase(context).knitPatternDao().saveNewPattern(knitPattern, thumbnail, context)
    }

    override fun onPostExecute(knitPattern: KnitPattern?) {
        super.onPostExecute(knitPattern)
        val callback = callback.get() ?: return
        if (knitPattern == null) {
            callback.onOpenKnitPatternFail()
        } else {
            callback.onKnitPatternOpened(knitPattern)
        }
    }
}
