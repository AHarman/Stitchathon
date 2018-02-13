package com.alexharman.stitchathon.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch

@Entity(tableName = "pattern_stitches",
        foreignKeys = [(ForeignKey(entity = KnitPatternInfoEntity::class, parentColumns = ["name"], childColumns = ["name"], onDelete = ForeignKey.CASCADE))])
internal class KnitPatternStitchesEntity(
        @PrimaryKey val name: String,
        @TypeConverters(KnitPatternConverters::class) val stitches: Array<Array<Stitch>>,
        @TypeConverters(KnitPatternConverters::class) val stitchTypes: Array<Stitch>) {

    constructor(knitPattern: KnitPattern) : this(knitPattern.name, knitPattern.stitches, knitPattern.stitchTypes)
}