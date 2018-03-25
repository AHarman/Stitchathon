package com.alexharman.stitchathon.database

import android.arch.persistence.room.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

@Dao
abstract class KnitPatternDao {

    fun savePatternChanges(knitPattern: KnitPattern) {
        insertKnitPatternEntity(KnitPatternEntity(knitPattern))
    }

    @Transaction
    open fun saveNewPattern(knitPattern: KnitPattern, thumbnail: Bitmap, context: Context) {
        val kpe = KnitPatternEntity(knitPattern)
        writeStitchesToFile(knitPattern, kpe.stitchesFilePath, context)
        writeBitmapToFile(thumbnail, kpe.thumbnailFilePath, context)
        insertKnitPatternEntity(kpe)
    }

    @Transaction
    open fun getKnitPattern(name: String, context: Context): KnitPattern {
        val result = selectKnitPattern(name)
        val stitches = readStitchesFromFile(result.stitchesFilePath, context)
        return KnitPattern(result.name, stitches, result.currentRow, result.nextStitchInRow)
    }

    @Transaction
    open fun getThumbnails(context: Context): HashMap<String, Bitmap> {
        val hashmap = HashMap<String, Bitmap>()
        selectAllKnitPatterns().forEach { kpe: KnitPatternEntity -> hashmap[kpe.name] = readBitmapFromFile(kpe.thumbnailFilePath, context) }
        return hashmap
    }

    @Transaction
    open fun deleteAllPatterns(context: Context) {
        val kpes = selectAllKnitPatterns()
        for (kpe in kpes) {
            deleteFiles(context, kpe)
            deleteKPE(kpe)
        }
    }

    @Transaction
    open fun deletePattern(name: String, context: Context) {
        val kpe = selectKnitPattern(name)
        deleteFiles(context, kpe)
        deleteKPE(kpe)
    }

    private fun deleteFiles(context: Context, kpe: KnitPatternEntity) {
        File(context.filesDir, kpe.stitchesFilePath).delete()
        File(context.filesDir, kpe.thumbnailFilePath).delete()
    }

    open fun getThumbnail(context: Context, name: String): Bitmap {
        return readBitmapFromFile(selectThumbnailFilePath(name), context)
    }

    private fun writeStitchesToFile(knitPattern: KnitPattern, path: String, context: Context) {
        try {
            val outputStream = context.openFileOutput(path, Context.MODE_PRIVATE)
            outputStream.write(Gson().toJson(knitPattern.stitches).toByteArray())
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
        val stringBuilder = StringBuilder()
        try {
            val inputStream = context.openFileInput(path)
            val reader = BufferedReader(InputStreamReader(inputStream))

            for (line in reader.readLine()) {
                stringBuilder.append(line)
            }
            inputStream.close()
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        return Gson().fromJson(stringBuilder.toString(), Array<Array<Stitch>>::class.java)
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
