package com.alexharman.stitchathon.database

import android.arch.persistence.room.TypeConverter
import com.alexharman.stitchathon.KnitPackage.Stitch
import com.google.gson.Gson

internal class KnitPatternConverters {

    @TypeConverter
    fun patternStitchesFromString(value: String): Array<Array<Stitch>> {
        return Gson().fromJson(value, Array<Array<Stitch>>::class.java)
    }

    @TypeConverter
    fun fromPatternStitches(stitches: Array<Array<Stitch>>): String {
        return Gson().toJson(stitches)
    }

    @TypeConverter
    fun stitchTypesFromString(value: String): Array<Stitch> {
        return Gson().fromJson(value, Array<Stitch>::class.java)
    }

    @TypeConverter
    fun fromStitchTypes(stitches: Array<Stitch>): String {
        return Gson().toJson(stitches)
    }
}