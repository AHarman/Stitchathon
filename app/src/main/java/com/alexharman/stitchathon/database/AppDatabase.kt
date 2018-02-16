package com.alexharman.stitchathon.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [KnitPatternEntity::class], version = 1)
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