package com.alexharman.stitchathon.repository.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alexharman.stitchathon.KnitPackage.KnitPattern

@Entity(tableName = "pattern_info")
internal data class KnitPatternEntity(
        @PrimaryKey val name: String,
        val currentRow: Int,
        val stitchesDoneInRow: Int,
        val oddRowsOpposite: Boolean,
        val stitchesFilePath: String,
        val thumbnailFilePath: String) {

    constructor(knitPattern: KnitPattern) : this(
            knitPattern.name,
            knitPattern.currentRow,
            knitPattern.stitchesDoneInRow,
            knitPattern.oddRowsOpposite,
            knitPattern.name + ".json",
            knitPattern.name + ".thumb.png")
}