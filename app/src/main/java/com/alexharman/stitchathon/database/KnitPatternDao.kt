package com.alexharman.stitchathon.database

import android.arch.persistence.room.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

@Dao
abstract class KnitPatternDao {

    fun savePatternChanges(knitPattern: KnitPattern) {
        Log.v("Database", "savePatternChanges: ${knitPattern.name}")
        updateKnitPatternEntity(KnitPatternEntity(knitPattern))
    }

    @Transaction
    open fun saveNewPattern(knitPattern: KnitPattern, thumbnail: Bitmap, context: Context) {
        Log.v("Database", "saveNewPattern: ${knitPattern.name}")
        val kpe = KnitPatternEntity(knitPattern)
        writeStitchesToFile(knitPattern, kpe.stitchesFilePath, context)
        writeBitmapToFile(thumbnail, kpe.thumbnailFilePath, context)
        insertKnitPatternEntity(kpe)
    }

    @Transaction
    open fun getKnitPattern(name: String, context: Context): KnitPattern {
        Log.v("Database", "getKnitPattern: $name")
        val result = selectKnitPattern(name)
        val stitches = readStitchesFromFile(result.stitchesFilePath, context)
        return KnitPattern(result.name, stitches, result.oddRowsOpposite, result.currentRow, result.stitchesDoneInRow)
    }

    @Transaction
    open fun getThumbnails(context: Context): Array<Pair<String, Bitmap>> {
        Log.v("Database", "Getting all thumbnails")
        val patterns = selectAllKnitPatterns()
        return Array(patterns.size,
                { i -> Pair(patterns[i].name, readBitmapFromFile(patterns[i].thumbnailFilePath, context)) })
    }

    @Transaction
    open fun deleteAllPatterns(context: Context) {
        Log.v("Database", "Deleting all patterns")
        for (kpe in selectAllKnitPatterns()) {
            deletePattern(kpe.name, context)
        }
    }

    @Transaction
    open fun deletePattern(name: String, context: Context) {
        Log.v("Database", "Deleting $name")
        val kpe = selectKnitPattern(name)
        deleteFiles(context, kpe)
        deleteKPE(kpe)
    }

    @Transaction
    open fun getThumbnail(context: Context, name: String): Bitmap {
        Log.v("Database", "Getting thumbnail for $name")
        return readBitmapFromFile(selectThumbnailFilePath(name), context)
    }

    private fun deleteFiles(context: Context, kpe: KnitPatternEntity) {
        File(context.filesDir, kpe.stitchesFilePath).delete()
        File(context.filesDir, kpe.thumbnailFilePath).delete()
    }

    private fun writeStitchesToFile(knitPattern: KnitPattern, path: String, context: Context) {
        try {
            val outputStream = context.openFileOutput(path, Context.MODE_PRIVATE)
            val sb = StringBuilder()
            knitPattern.stitches.map {
                row -> row.map { col -> sb.append(col.toString());sb.append(",") }
                sb.replace(sb.lastIndex, sb.length, "\n")
            }
            outputStream.write(sb.toString().toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun writeBitmapToFile(thumbnail: Bitmap, path: String, context: Context) {
        try {
            val outputStream = context.openFileOutput(path, Context.MODE_PRIVATE)
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun readStitchesFromFile(path: String, context: Context): Array<Array<Stitch>> {
        val stitches = ArrayList<Array<Stitch>>()
        try {
            val inputStream = context.openFileInput(path)
            val reader = BufferedReader(InputStreamReader(inputStream))
            for (line in reader.lineSequence()) {
                stitches.add(line.trim().split(",").map {
                        Stitch(it)
                    }.toTypedArray())
            }
            inputStream.close()
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        return stitches.toTypedArray()
    }

    private fun readBitmapFromFile(path: String, context: Context): Bitmap {
        val bitmap: Bitmap
        val pathUri = Uri.fromFile(File(context.filesDir.toString() + "/" + path))
        val opts = BitmapFactory.Options()
        opts.inMutable = true
        try {
            val inputStream = context.contentResolver.openInputStream(pathUri)
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return bitmap
    }

    @Update
    internal abstract fun updateKnitPatternEntity(knitPatternEntity: KnitPatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertKnitPatternEntity(knitPatternEntity: KnitPatternEntity)

    @Delete
    internal abstract fun deleteKPE(knitPatternEntity: KnitPatternEntity)

    @Query("SELECT thumbnailFilePath FROM pattern_info WHERE name LIKE :name")
    internal abstract fun selectThumbnailFilePath(name: String): String

    @Query("SELECT name FROM pattern_info;")
    abstract fun getPatternNames(): Array<String>

    @Query("SELECT * from pattern_info")
    internal abstract fun selectAllKnitPatterns(): Array<KnitPatternEntity>

    @Query("SELECT * FROM pattern_info WHERE name LIKE :name;")
    internal abstract fun selectKnitPattern(name: String): KnitPatternEntity
}
