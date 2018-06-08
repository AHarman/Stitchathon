package com.alexharman.stitchathon.databaseAccessAsyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPatternDrawer
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.database.AppDatabase
import java.lang.ref.WeakReference

internal abstract class ImportPatternTask<V> internal constructor(context: Context, callback: OpenPattern) : AsyncTask<V, String, KnitPattern>() {
    protected var context: WeakReference<Context> = WeakReference(context)
    private var callBack: WeakReference<OpenPattern> = WeakReference(callback)
    private lateinit var knitPatternDrawer: KnitPatternDrawer
    private lateinit var thumbnail: Bitmap

    internal fun saveNewPattern(knitPattern: KnitPattern) {
        val context = context.get() ?: return
        knitPatternDrawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context))
        thumbnail = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)
        AppDatabase.getAppDatabase(context).knitPatternDao().saveNewPattern(knitPattern, thumbnail, context)
    }

    override fun onPostExecute(pattern: KnitPattern?) {
        super.onPostExecute(pattern)
        if (pattern != null) {
            callBack.get()?.onPatternReturned(pattern, knitPatternDrawer, thumbnail)
        }
    }
}
