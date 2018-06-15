package com.alexharman.stitchathon.repository.database.asyncTasks

import android.os.AsyncTask
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.database.KnitPatternDao

class SavePatternChangesTask(private val dao: KnitPatternDao) : AsyncTask<KnitPattern, Void, Void>() {
    override fun doInBackground(vararg knitPatterns: KnitPattern): Void? {
        dao.savePatternChanges(knitPatterns[0])
        return null
    }
}