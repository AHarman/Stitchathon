package com.alexharman.stitchathon.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.alexharman.stitchathon.KnitPackage.KnitPattern

@Entity(tableName = "pattern_info")
internal data class KnitPatternEntity(@PrimaryKey val name: String, val currentRow: Int, val nextStitchInRow: Int, val filePath: String) {
    constructor(knitPattern: KnitPattern) : this(
            knitPattern.name,
            knitPattern.currentRow,
            knitPattern.nextStitchInRow,
            knitPattern.name + ".json")
}