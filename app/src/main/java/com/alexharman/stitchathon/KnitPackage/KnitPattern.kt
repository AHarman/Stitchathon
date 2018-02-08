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

    fun incrementRow(): Int {
        if (isFinished)
            return 0
        var stitchesDone = 0
        do {
            increment()
            stitchesDone++
        } while (!isStartOfRow && !isFinished)
        return stitchesDone
    }

    fun undoStitch() {
        if (currentRow == 0 && nextStitchInRow == startOfRow)
            return

        if (isStartOfRow) {
            currentRow--
            nextStitchInRow = endOfRow
            currentDistanceInRow = stitches[currentRow].sumBy { it.width } - stitches[currentRow][nextStitchInRow].width
        } else {
            nextStitchInRow -= rowDirection
            currentDistanceInRow -= stitches[currentRow][nextStitchInRow].width
        }
        totalStitchesDone--
    }

    val rowDirection: Int
        get() = if (oddRowsOpposite && (currentRow % 2 == 1)) -1 else 1

    val isEndOfRow: Boolean
        get() = if (rowDirection == 1) nextStitchInRow == stitches[currentRow].size else (nextStitchInRow == -1)

    val isStartOfRow: Boolean
        get() = if (rowDirection == 1) nextStitchInRow == 0 else (nextStitchInRow == stitches[currentRow].size - 1)

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
