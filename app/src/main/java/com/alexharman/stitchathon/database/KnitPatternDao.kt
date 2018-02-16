package com.alexharman.stitchathon.database

import android.arch.persistence.room.*
import android.content.Context
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Dao
abstract class KnitPatternDao {
    fun savePatternChanges(knitPattern: KnitPattern) {
        insertKnitPatternEntity(KnitPatternEntity(knitPattern))
    }

    @Transaction
    open fun saveNewPattern(knitPattern: KnitPattern, context: Context) {
        val kpe = KnitPatternEntity(knitPattern)
        writeStitchesToFile(knitPattern, kpe.filePath, context)
        insertKnitPatternEntity(kpe)
    }

    @Transaction
    open fun getKnitPattern(name: String, context: Context): KnitPattern {
        val result = selectKnitPattern(name)
        val stitches = readStitchesFromFile(result.filePath, context)
        return KnitPattern(result.name, stitches, result.currentRow, result.nextStitchInRow)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertKnitPatternEntity(knitPatternEntity: KnitPatternEntity)

    @Delete
    internal abstract fun delete(knitPatternEntity: KnitPatternEntity)

    @Query("SELECT name FROM pattern_info;")
    abstract fun getPatternNames(): Array<String>

    @Query("SELECT * FROM pattern_info WHERE name LIKE :name;")
    internal abstract fun selectKnitPattern(name: String): KnitPatternEntity
}
