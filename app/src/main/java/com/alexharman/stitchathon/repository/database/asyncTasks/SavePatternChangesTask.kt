package com.alexharman.stitchathon.repository.database.asyncTasks

import android.os.AsyncTask
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.MainActivity

class SavePatternChangesTask : AsyncTask<KnitPattern, Void, Void>() {
    override fun doInBackground(vararg knitPatterns: KnitPattern): Void? {
        MainActivity.db.knitPatternDao().savePatternChanges(knitPatterns[0])
        return null
    }
}