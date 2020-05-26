package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.pattern.drawer.KnitPatternDrawer
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference
import kotlin.math.min

internal abstract class ImportPatternTask<V> internal constructor(context: Context, listener: KnitPatternDataSource.ImportPatternListener?)
    : AsyncTask<V, String, KnitPattern>() {
    protected var context: WeakReference<Context> = WeakReference(context)
    private var callback: WeakReference<KnitPatternDataSource.ImportPatternListener?> = WeakReference(listener)

    // TODO: Move to repo.
    internal fun saveNewPattern(knitPattern: KnitPattern) {
        val context = context.get() ?: return
        val thumbnail = createPatternThumbnail(knitPattern, context)
        AppDatabase.getAppDatabase(context).knitPatternDao().saveNewPattern(knitPattern, thumbnail, context)
    }

    // TODO: Move elsewhere
    private fun createPatternThumbnail(knitPattern: KnitPattern, context: Context): Bitmap {
        val drawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context))
        val bitmap = Bitmap.createBitmap(drawer.overallWidth, drawer.overallHeight, Bitmap.Config.ARGB_8888)
        val width = min(drawer.overallWidth, drawer.overallHeight * 3)
        val height = min(drawer.overallHeight, drawer.overallWidth * 3)

        drawer.draw(
                Canvas(bitmap),
                Rect(0, 0, drawer.overallWidth, drawer.overallHeight),
                Rect(0, 0, drawer.overallWidth, drawer.overallHeight))

        return ThumbnailUtils.extractThumbnail(bitmap, 200, 200)
    }

    override fun onPostExecute(knitPattern: KnitPattern?) {
        super.onPostExecute(knitPattern)
        val callback = callback.get() ?: return
        if (knitPattern == null) {
            callback.onPatternImportFail()
        } else {
            callback.onPatternImport(knitPattern)
        }
    }
}
