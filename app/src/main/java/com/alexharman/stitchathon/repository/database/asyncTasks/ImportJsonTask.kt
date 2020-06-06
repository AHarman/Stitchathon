package com.alexharman.stitchathon.repository.database.asyncTasks

import android.content.Context
import android.net.Uri
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternParser
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

internal class ImportJsonTask(
        context: Context,
        listener: KnitPatternDataSource.ImportPatternListener?,
        private val uri: String,
        private val name: String,
        private val oddRowsOpposite: Boolean):
        ImportPatternTask(context, listener) {

    override fun doInBackground(vararg voids: Void): KnitPattern? {
        var knitPattern: KnitPattern? = null
        try {
            knitPattern = KnitPatternParser.createKnitPattern(readTextFile(Uri.parse(uri)))
            saveNewPattern(knitPattern)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return knitPattern
    }

    private fun readTextFile(uri: Uri): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = context.get()!!.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String
            while (true) {
                line = reader.readLine() ?: break
                stringBuilder.append(line)
            }
            inputStream?.close()
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return stringBuilder.toString()
    }
}
