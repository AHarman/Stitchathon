package com.alexharman.stitchathon.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.alexharman.stitchathon.KnitPackage.Stitch
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference

@Database(entities = [KnitPatternEntity::class], version = 2)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun knitPatternDao(): KnitPatternDao

    companion object {
        private var instance: AppDatabase? = null
        private lateinit var context: WeakReference<Context>

        fun getAppDatabase(context: Context): AppDatabase {
            if (instance == null) {
                this.context = WeakReference(context)
                instance = Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
                        .addMigrations(MIGRATION_1_2)
                        .build()
            }
            return instance as AppDatabase
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT stitchesFilePath FROM pattern_info")
                while (cursor.moveToNext()) {
                    val stitches = readStitchesFromFile(cursor.getString(0), context.get()!!)
                    writeStitchesToFile(stitches, cursor.getString(0), context.get()!!)
                }
            }

            private fun writeStitchesToFile(stitches: Array<Array<Stitch>>, path: String, context: Context) {
                try {
                    val outputStream = context.openFileOutput(path, Context.MODE_PRIVATE)
                    val sb = StringBuilder()
                    stitches.map { row ->
                        row.map { col -> sb.append(col);sb.append(",") }
                        sb.replace(sb.lastIndex, sb.length, "\n")
                    }
                    outputStream.write(sb.toString().toByteArray())
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
        }
    }
}