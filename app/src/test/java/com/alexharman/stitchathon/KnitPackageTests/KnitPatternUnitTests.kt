package com.alexharman.stitchathon.KnitPackageTests

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.Stitch
import org.junit.Assert.assertEquals
import org.junit.Test

class KnitPatternUnitTests {

    private val testPatternWidth = 20
    private val testPatternHeight = 10
    private val testStrings = Array(testPatternHeight, { row -> Array(testPatternWidth, { col -> "${(col + row) % 3}" } ) } )
    private val testStitches = testStrings.map { row -> row.map { Stitch(it) }.toTypedArray() }.toTypedArray()

    private val testSplitStrings = Array(testPatternHeight, { row -> Array(testPatternWidth, { col -> "${col % 3}/${row % 3}" } ) } )

    @Test
    fun givenSplitStrings_thenStitchTypeContainsSubTypes() {
        val pattern = KnitPattern(testSplitStrings)
        val expected = setOf(
                "0", "1", "2",
                "0/0", "0/1", "0/2",
                "1/0", "1/1", "1/2",
                "2/0", "2/1", "2/2").map { Stitch(it) }.toSet()

        assert(expected == pattern.stitchTypes.toSet())
    }

    @Test
    fun ifOnForwardRow_correctStitchesDoneInRow() {
        val initialNextStitchInRow = 7
        val initialRow = 4
        val expected = initialNextStitchInRow

        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun ifOnReverseRow_correctStitchesDoneInRow() {
        val initialNextStitchInRow = 5
        val initialRow = 1
        val expected = testPatternWidth - 1 - initialNextStitchInRow

        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun ifOnForwardRow_correctInitialTotalStitchesDone() {
        val initialNextStitchInRow = 7
        val initialRow = 4
        val expected = initialNextStitchInRow + (initialRow * testPatternWidth)

        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)

        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenOddRowsOpposite_ifOnReverseRow_correctInitialTotalStitchesDone() {
        val initialNextStitchInRow = 5
        val initialRow = 1

        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)

        val expected = testPatternWidth - initialNextStitchInRow - 1 + (initialRow * testPatternWidth)
        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenOddRowsOpposite_ifOnReverseRow_thenCorrectRowDirection() {
        val pattern = KnitPattern("", testStitches, 1, 0)

        assertEquals(-1, pattern.rowDirection)
    }

    @Test
    fun givenOddRowsOpposite_ifOnForwardRow_thenCorrectRowDirection() {
        val pattern = KnitPattern("", testStitches, 4, 0)

        assertEquals(1, pattern.rowDirection)
    }

    @Test
    fun givenOddRowsForward_ifOnForwardRow_thenCorrectRowDirection() {
        assert(false)
    }

    @Test
    fun givenOnForwardRow_thenNextStitchInRowCorrect() {
        val initialNextStitchInRow = 5
        val expected = initialNextStitchInRow
        val pattern = KnitPattern("", testStitches, 0, initialNextStitchInRow)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenOnReverseRow_thenNextStitchInRowCorrect() {
        val initialNextStitchInRow = 5
        val expected = testPatternWidth - initialNextStitchInRow - 1
        val pattern = KnitPattern("", testStitches, 1, initialNextStitchInRow)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test fun givenOnLastRowAndEndOfRow_thenIsFinished() {
        val pattern = KnitPattern("", testStitches, testPatternHeight - 1, 0)
        pattern.increment()

        assert(pattern.isFinished)
    }

    @Test fun givenIsFinished_ifIncrement_doNothing() {
        val pattern = KnitPattern("", testStitches, testPatternHeight - 1, 0)
        pattern.increment()
        val expected = pattern.totalStitchesDone

        pattern.increment()

        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenEnoughSpaceInEvenRow_ifIncrementStitches_thenCorrectAmountDone() {
        val pattern = KnitPattern(testStrings)
        val stitchesToIncrement = 5
        val expected = pattern.stitchesDoneInRow + stitchesToIncrement

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenEnoughSpaceInReverseRow_ifIncrementStitches_thenCorrectAmountDone() {
        val initialNextStitchInRow = 7
        val pattern = KnitPattern("", testStitches, 1, initialNextStitchInRow)
        val stitchesToIncrement = 5
        val expected = pattern.stitchesDoneInRow + stitchesToIncrement

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenNotEnoughSpaceInForwardRow_ifIncrementStitches_thenCorrectOverflow() {
        val initialNextStitchInRow = testPatternWidth - 5
        val stitchesToIncrement = 8
        val expected = (initialNextStitchInRow + stitchesToIncrement) % testPatternWidth
        val pattern = KnitPattern("", testStitches, 0, initialNextStitchInRow)

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenNotEnoughSpaceInReverseRow_ifIncrementStitches_thenCorrectOverflow() {
        val initialNextStitchInRow = 5
        val stitchesToIncrement = 8
        val expected = stitchesToIncrement - initialNextStitchInRow - 1
        val pattern = KnitPattern("", testStitches, 1, initialNextStitchInRow)

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenNotEnoughSpaceInForwardRow_ifIncrementStitches_thenRowIncremented() {
        val initialNextStitch = testPatternWidth - 5
        val stitchesToIncrement = 8
        val initialRow = 0
        val expected = initialRow + 1
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitch)

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.currentRow)
    }

    @Test
    fun givenNotEnoughSpaceInReverseRow_ifIncrementStitches_thenRowIncremented() {
        val initialNextStitch = 5
        val stitchesToIncrement = 8
        val initialRow = 1
        val expected = initialRow + 1
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitch)

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.currentRow)
    }

    @Test
    fun givenOnLastRow_ifIncrementToEndOfRow_thenFinish() {
        val initialNextStitch = testPatternWidth / 2
        val stitchesToIncrement = testPatternWidth - initialNextStitch
        val initialRow = testPatternHeight - 1
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitch)

        pattern.increment(stitchesToIncrement)

        assert(pattern.isFinished)
    }

    @Test
    fun givenOnLastRow_ifIncrementPastEndOfRow_thenNoOverflow() {
        val initialNextStitch = testPatternWidth / 2
        val stitchesToIncrement = testPatternWidth
        val initialRow = testPatternHeight - 1
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitch)

        pattern.increment(stitchesToIncrement)

        assertEquals(testPatternHeight - 1, pattern.currentRow)
    }

    @Test
    fun ifIncrementStitches_thenTotalStitchesDoneCorrect() {
        val initialNextStitchInRow = testPatternWidth / 2
        val initialRow = 3
        val stitchesToIncrement = testPatternWidth
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)
        val expected = pattern.totalStitchesDone + stitchesToIncrement

        pattern.increment(stitchesToIncrement)

        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenOnForwardRow_ifIncrementRow_thenRowIncremented() {
        val pattern = KnitPattern("", testStitches, 0, 0)

        pattern.incrementRow()

        assertEquals(1, pattern.currentRow)
    }

    @Test
    fun givenOnForwardRow_ifIncrementRow_then0StitchesDoneInRow() {
        val pattern = KnitPattern("", testStitches, 0, 0)

        pattern.incrementRow()

        assertEquals(0, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenOnReverseRow_ifIncrementRow_thenRowIncremented() {
        val pattern = KnitPattern("", testStitches, 1, 0)

        pattern.incrementRow()

        assertEquals(2, pattern.currentRow)
    }

    @Test
    fun givenOnReverseRow_ifIncrementRow_then0StitchesDoneInRow() {
        val pattern = KnitPattern("", testStitches, 1, 0)

        pattern.incrementRow()

        assertEquals(0, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenOnLastRow_ifIncrementRow_thenFinish() {
        val initialNextStitch = 5
        val initialRow = testPatternHeight - 1
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitch)

        pattern.incrementRow()

        assert(pattern.isFinished)
    }

    @Test
    fun givenEnoughSpaceInForwardRow_ifUndoStitch_thenCorrectAmountDone() {
        val initialNextStitch = 5
        val expected = initialNextStitch - 1
        val pattern = KnitPattern("", testStitches, 0, initialNextStitch)

        pattern.undoStitch()

        assertEquals(expected, pattern.nextStitchInRow)
    }

    @Test
    fun givenEnoughSpaceInReverseRow_ifUndoStitches_thenCorrectAmountDone() {
        val initialNextStitch = 5
        val pattern = KnitPattern("", testStitches, 0, initialNextStitch)
        val expected = pattern.stitchesDoneInRow - 1

        pattern.undoStitch()

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenAtStartOfForwardRow_ifUndoStitch_thenDecrementRow() {
        val initialRow = 2
        val expected = initialRow - 1
        val pattern = KnitPattern("", testStitches, initialRow, 0)

        pattern.undoStitch()

        assertEquals(expected, pattern.currentRow)
    }

    @Test
    fun givenAtStartOfForwardRow_ifUndoStitch_thenCorrectAmountDone() {
        val pattern = KnitPattern("", testStitches, 2, 0)
        val expected = pattern.totalStitchesDone - 1

        pattern.undoStitch()

        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenAtStartOfReverseRow_ifUndoStitch_thenDecrementRow() {
        val initialRow = 1
        val expected = initialRow - 1
        val pattern = KnitPattern("", testStitches, initialRow, testPatternWidth - 1)

        pattern.undoStitch()

        assertEquals(expected, pattern.currentRow)
    }

    @Test
    fun givenAtStartOfReverseRow_ifUndoStitch_thenCorrectAmountDone() {
        val initialRow = 1
        val expected = testPatternWidth - 1
        val pattern = KnitPattern("", testStitches, initialRow, testPatternWidth - 1)

        pattern.undoStitch()

        assertEquals(expected, pattern.stitchesDoneInRow)
    }

    @Test
    fun givenStartOfFirstRow_ifUndoStitches_thenDontDecrementRow() {
        val pattern = KnitPattern("", testStitches, 0, 0)

        pattern.undoStitch()

        assertEquals(0, pattern.currentRow)
    }

    @Test
    fun givenStartOfFirstRow_ifUndoStitches_then0StitchesDone() {
        val pattern = KnitPattern("", testStitches, 0, 0)

        pattern.undoStitch()

        assertEquals(0, pattern.stitchesDoneInRow)
    }

    @Test
    fun ifUndoStitches_thenTotalStitchesDoneCorrect() {
        val initialNextStitchInRow = 5
        val initialRow = 7
        val pattern = KnitPattern("", testStitches, initialRow, initialNextStitchInRow)
        val expected = pattern.totalStitchesDone - 1
        pattern.undoStitch()

        assertEquals(expected, pattern.totalStitchesDone)
    }

    @Test
    fun givenOnForwardRow_thenStitchesLeftCorrect() {
        val initialNextStitchInRow = 6
        val pattern = KnitPattern("", testStitches, 4, initialNextStitchInRow)
        val expected = pattern.stitchesDoneInRow - initialNextStitchInRow

        assertEquals(expected, pattern.stitchesLeftInRow)
    }

    @Test
    fun givenOnReverseRow_thenStitchesLeftCorrect() {
        val initialNextStitchInRow = 6
        val pattern = KnitPattern("", testStitches, 5, initialNextStitchInRow)
        val expected = pattern.stitchesDoneInRow - initialNextStitchInRow

        assertEquals(expected, pattern.stitchesLeftInRow)
    }

    @Test
    fun givenOnForwardRow_ifAtStartOfRow_isStartOfRow() {
        val pattern = KnitPattern("", testStitches, 4, 0)

        assert(pattern.isStartOfRow)
    }

    @Test
    fun givenOnReverseRow_ifAtStartOfRow_isStartOfRow() {
        val pattern = KnitPattern("", testStitches, 5, testPatternWidth - 1)

        assert(pattern.isStartOfRow)
    }

    @Test
    fun givenOnForwardRow_ifNotAtStartOfRow_isStartOfRow() {
        val pattern = KnitPattern("", testStitches, 4, 6)

        assert(!pattern.isStartOfRow)
    }

    @Test
    fun givenOnReverseRow_ifNotAtStartOfRow_isStartOfRow() {
        val pattern = KnitPattern("", testStitches, 5, 0)

        assert(!pattern.isStartOfRow)
    }
}