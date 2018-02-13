package com.alexharman.stitchathon.KnitPackage

class KnitPattern {

    val name: String
    val stitches: Array<Array<Stitch>>
    val stitchTypes: Array<Stitch>
    val totalStitches: Int
    val patternWidth: Int

    // Takes width of stitches into account
    var currentDistanceInRow = 0
        private set
    var nextStitchInRow = 0
        private set
    var totalStitchesDone = 0
        private set
    var currentRow = 0
        private set

    // Assuming doubleknit for now. Will be false for knitting on the round
    val oddRowsOpposite = true

    constructor(name: String, stitches: Array<Array<Stitch>>, stitchTypes: Array<Stitch>, currentRow: Int = 0, nextStitchInRow: Int = 0) {
        this.name = name
        this.stitches = stitches
        this.stitchTypes = stitchTypes
        this.currentRow = currentRow
        this.nextStitchInRow = nextStitchInRow
        totalStitches = findTotalStitches(stitches)
        patternWidth = findPatternWidth(stitches)
        totalStitchesDone = findTotalStitchesDone(stitches, currentRow)
        currentDistanceInRow = findCurrentDistanceInRow(stitches, currentRow)
    }

    constructor(pattern: Array<Array<String>>, name: String = "") {
        stitches = buildPattern(pattern)
        totalStitches = findTotalStitches(stitches)
        patternWidth = findPatternWidth(stitches)
        stitchTypes = buildStitchTypes(stitches)
        this.name = name
    }

    constructor(pattern: ArrayList<ArrayList<String>>, name: String = "") : this(Array(pattern.size, { i -> pattern[i].toTypedArray() }), name)

    private fun buildPattern(pattern: Array<Array<String>>): Array<Array<Stitch>> {
        return Array(pattern.size, { row ->
            Array(pattern[row].size, { col ->
                Stitch(pattern[row][col])
            })
        })
    }


    private fun buildStitchTypes(stitches: Array<Array<Stitch>>) =  stitches
            .flatten()
            .flatMap { if (it.isSplit) it.madeOf.toList() + listOf(it) else listOf(it) }
            .distinct()
            .toTypedArray()

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

    private fun findTotalStitches(stitches: Array<Array<Stitch>>) = stitches.sumBy { it.size }

    private fun findPatternWidth(stitches: Array<Array<Stitch>>) =
            stitches.maxBy { it.sumBy { it.width } }?.sumBy { it.width } ?: 0

    private fun findTotalStitchesDone(stitches: Array<Array<Stitch>>, currentRow: Int): Int {
        var totalStitchesDone = 0
        if (currentRow > 0)
            totalStitchesDone += stitches.sliceArray(IntRange(0, currentRow - 1)).sumBy { it.sumBy { it.width } }
        if (nextStitchInRow > 0)
            totalStitchesDone += stitches[currentRow].sliceArray(IntRange(0, nextStitchInRow - 1)).size
        return totalStitchesDone
    }

    private fun findCurrentDistanceInRow(stitches: Array<Array<Stitch>>, currentRow: Int): Int =
            stitches[currentRow].sliceArray(IntRange(0, nextStitchInRow - 1)).sumBy { it.width }

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
