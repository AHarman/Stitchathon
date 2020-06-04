package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.os.AsyncTask
import androidx.preference.PreferenceManager
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.pattern.drawer.KnitPatternDrawer
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.PreferenceKeys
import com.alexharman.stitchathon.repository.database.AppDatabase
import java.lang.ref.WeakReference

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
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        // TODO: Use repo instead of repeating what's done there
        val patternPrefs =  KnitPatternPreferences(
                backgroundColor = prefs.getInt(PreferenceKeys.BACKGROUND_COLOUR, R.color.default_pattern_background_colour),
                stitchColors = arrayOf(
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_1, context.getColor(R.color.default_stitch_colour_1)),
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_2, context.getColor(R.color.default_stitch_colour_2)),
                        prefs.getInt(PreferenceKeys.STITCH_COLOUR_3, context.getColor(R.color.default_stitch_colour_3))
                ),
                stitchPad = prefs.getInt(PreferenceKeys.STITCH_PAD, context.resources.getInteger(R.integer.default_stitch_pad)),
                stitchSize = prefs.getInt(PreferenceKeys.STITCH_SIZE, context.resources.getInteger(R.integer.default_stitch_size))
        )
        val drawer = KnitPatternDrawer(knitPattern, patternPrefs)
        val bitmap = Bitmap.createBitmap(drawer.overallWidth, drawer.overallHeight, Bitmap.Config.ARGB_8888)

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
