package com.alexharman.stitchathon.KnitPackage

import java.util.ArrayList

class KnitPattern {

    lateinit var stitches: Array<Array<Stitch>>
    lateinit var name: String

    // Takes width of stitches into account
    var currentDistanceInRow = 0
        private set
    var nextStitchInRow = 0
        private set
    var totalStitches = 0
        private set
    var totalStitchesDone = 0
        private set
    var currentRow = 0
        private set
    var patternWidth = 0
        private set
    lateinit var stitchTypes: Array<Stitch>

    // Assuming doubleknit for now. Will be false for knitting on the round
    private val oddRowsOpposite = true

    constructor(pattern: Array<Array<String>>) {
        buildPattern(pattern)
    }

    constructor(pattern: ArrayList<ArrayList<String>>, name: String) : this(pattern) {
        this.name = name
    }

    constructor(pattern: ArrayList<ArrayList<String>>) : this(Array(pattern.size, { i -> pattern[i].toTypedArray() }))

    constructor(pattern: Array<Array<String>>, name: String) : this(pattern) {
        this.name = name
    }

    private fun buildPattern(pattern: Array<Array<String>>) {
        stitches = Array(pattern.size, { row ->
            Array(pattern[row].size, { col ->
                Stitch(pattern[row][col])
            })
        })
        totalStitches = stitches.sumBy { it.size }
        patternWidth = stitches.maxBy { it.sumBy { it.width } }?.sumBy { it.width } ?: 0
        stitchTypes = stitches
                .flatten()
                .flatMap { if (it.isSplit) it.madeOf.toList() + listOf(it) else listOf(it) }
                .distinct()
                .toTypedArray()
    }

    // TODO: Combine next couple of functions maybe? Have a "do n stitches?"
    // TODO: Either way, these two need work

    fun increment () {
        if (isFinished)
            return
        currentDistanceInRow += stitches[currentRow][nextStitchInRow].width
        totalStitchesDone++
        nextStitchInRow += rowDirection

        if (!isFinished && isEndOfRow) {
            currentRow++
            currentDistanceInRow = 0
            nextStitchInRow = startOfRow
        }
    }

    // TODO: Causes outOfBounds exception if only 1 row left to fill
    fun incrementRow(): Int {
        if (currentRow == stitches.size - 1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.size - 1].size)) {
            return 0
        }
        var newStitchesDone = 0
        val direction = rowDirection

        var i = nextStitchInRow
        while (i * direction <= endOfRow) {
            newStitchesDone++
            i += direction
        }

        totalStitchesDone += newStitchesDone
        currentRow++
        nextStitchInRow = startOfRow
        currentDistanceInRow = 0
        return newStitchesDone
    }

    fun undoStitch() {
        if (currentRow == 0 && nextStitchInRow == startOfRow) {
            return
        }

        if (isStartOfRow) {
            currentRow--
            nextStitchInRow = endOfRow
            currentDistanceInRow = 0
            var c = Math.min(startOfRow, endOfRow)
            while (c < Math.max(startOfRow, endOfRow)) {
                currentDistanceInRow += stitches[currentRow][c].width
                c += 1
            }
        } else {
            currentDistanceInRow -= stitches[currentRow][nextStitchInRow].width
            nextStitchInRow -= rowDirection
        }
        totalStitchesDone--
    }

    val rowDirection: Int
        get() = if (oddRowsOpposite && (currentRow % 2 == 1)) -1 else 1


    val isEndOfRow: Boolean
        get() {
            return if (rowDirection == 1) {
                nextStitchInRow == stitches[currentRow].size
            } else {
                nextStitchInRow == -1
            }
        }

    val isStartOfRow: Boolean
        get() {
            return if (rowDirection == 1) {
                nextStitchInRow == 0
            } else nextStitchInRow == stitches[currentRow].size - 1
        }

    private val endOfRow: Int
        get() = if (rowDirection == 1) (stitches[currentRow].size - 1) else 0

    private val startOfRow: Int
        get() = if (rowDirection == 1) 0 else (stitches[currentRow].size - 1)

    val stitchesLeftInRow: Int
        get() = if (rowDirection == 1) (stitches[currentRow].size - nextStitchInRow) else (nextStitchInRow + 1)

    val stitchesDoneInRow: Int
        get() = if (rowDirection == 1) nextStitchInRow else (stitches[currentRow].size - nextStitchInRow - 1)

    val numRows: Int
        get() = stitches.size

    val isFinished: Boolean
        get() = currentRow == numRows - 1 && isEndOfRow
}
