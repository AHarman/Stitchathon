package com.alexharman.stitchathon.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.alexharman.stitchathon.KnitPackage.KnitPattern

@Entity(tableName = "pattern_info")
internal data class KnitPatternEntity(@PrimaryKey val name: String, val currentRow: Int, val stitchesDoneInRow: Int, val stitchesFilePath: String, val thumbnailFilePath: String) {
    constructor(knitPattern: KnitPattern) : this(
            knitPattern.name,
            knitPattern.currentRow,
            knitPattern.stitchesDoneInRow,
            knitPattern.name + ".json",
            knitPattern.name + ".thumb.png")
}