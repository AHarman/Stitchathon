package com.alexharman.stitchathon.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [KnitPatternInfoEntity::class, KnitPatternStitchesEntity::class], version = 1)
@TypeConverters(KnitPatternConverters::class)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun knitPatternDao(): KnitPatternDao

    companion object {
        private var instance: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, "app-database").build()
            }
            return instance as AppDatabase
        }
    }
}