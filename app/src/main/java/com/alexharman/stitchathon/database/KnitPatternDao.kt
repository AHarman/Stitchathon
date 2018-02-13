package com.alexharman.stitchathon.database

import android.arch.persistence.room.*

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch

@Dao
abstract class KnitPatternDao {

    @Transaction
    open fun saveNewPattern(knitPattern: KnitPattern) {
        insertKnitPatternInfoEntity(KnitPatternInfoEntity(knitPattern))
        insertKnitPatternStitchEntity(KnitPatternStitchesEntity(knitPattern))
    }

    fun savePattern(knitPattern: KnitPattern) {
        insertKnitPatternInfoEntity(KnitPatternInfoEntity(knitPattern))
    }

    fun getKnitPattern(name: String): KnitPattern {
        val result = selectKnitPattern(name)
        return KnitPattern(result.name, result.stitches, result.stitchTypes, result.currentRow, result.nextStitchInRow)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertKnitPatternInfoEntity(knitPatternInfoEntity: KnitPatternInfoEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    internal abstract fun insertKnitPatternStitchEntity(knitPatternStitchesEntity: KnitPatternStitchesEntity)

    @Delete
    internal abstract fun delete(knitPatternInfoEntity: KnitPatternInfoEntity)

    @Query("SELECT name FROM pattern_info;")
    abstract fun getPatternNames(): Array<String>

    @Query("SELECT pattern_info.*, pattern_stitches.stitches, pattern_stitches.stitchTypes " +
            "FROM pattern_info, pattern_stitches " +
            "WHERE pattern_info.name LIKE :name " +
            "AND pattern_stitches.name LIKE :name;")
    protected abstract fun selectKnitPattern(name: String): InfoAndStitches

    protected class InfoAndStitches(val name: String,
                                    val currentRow: Int,
                                    val nextStitchInRow: Int,
                                    val stitches: Array<Array<Stitch>>,
                                    val stitchTypes: Array<Stitch>)
}
