package com.alexharman.stitchathon.KnitPackage

class KnitPattern(
        val name: String,
        val stitches: Array<Array<Stitch>>,
        val oddRowsOpposite: Boolean = true,
        currentRow: Int = 0,
        stitchesDoneInRow: Int = 0) {

    val stitchTypes: Set<Stitch>
    val totalStitches: Int
    val patternWidth: Int

    // Takes width of stitches into account
    var currentDistanceInRow = 0
        private set
    var stitchesDoneInRow = 0
        private set
    var totalStitchesDone = 0
        private set
    var currentRow = 0
        private set

    init {
        this.currentRow = currentRow
        this.stitchesDoneInRow = stitchesDoneInRow
        totalStitches = findTotalStitches(stitches)
        patternWidth = findPatternWidth(stitches)
        totalStitchesDone = findTotalStitchesDone(stitches, currentRow)
        currentDistanceInRow = findCurrentDistanceInRow(stitches, currentRow)

        val initStitchTypes = stitches.flatten().toSet()
        stitchTypes = initStitchTypes.union(initStitchTypes.flatMap { it.madeOf.toList() }.toSet())
    }

    constructor(name: String = "", pattern: Array<Array<String>>, oddRowsOpposite: Boolean = true) :
            this (name, pattern.map { row -> row.map { Stitch(it) }.toTypedArray() }.toTypedArray(), oddRowsOpposite)

    constructor(name: String = "", pattern: List<List<String>>, oddRowsOpposite: Boolean = true) :
            this(name, Array(pattern.size, { i -> pattern[i].toTypedArray() }), oddRowsOpposite)

    fun increment (numStitches: Int = 1) {
        for (i in 0 until numStitches) {
            if (isFinished) return

            currentDistanceInRow += stitches[currentRow][nextStitchInRow].width
            totalStitchesDone++
            stitchesDoneInRow++

            if (!isFinished && isEndOfRow) {
                currentRow++
                currentDistanceInRow = 0
                stitchesDoneInRow = 0
            }
        }
    }

    fun incrementRow() = increment(stitchesLeftInRow)

    fun undoStitch() {
        if (currentRow == 0 && isStartOfRow)
            return

        if (isStartOfRow) {
            currentRow--
            stitchesDoneInRow = stitches[currentRow].size - 1
            currentDistanceInRow = stitches[currentRow].sumBy { it.width } - stitches[currentRow][nextStitchInRow].width
        } else {
            stitchesDoneInRow--
            currentDistanceInRow -= stitches[currentRow][nextStitchInRow].width
        }
        totalStitchesDone--
    }

    private fun findTotalStitches(stitches: Array<Array<Stitch>>) = stitches.sumBy { it.size }

    private fun findPatternWidth(stitches: Array<Array<Stitch>>) =
            stitches.maxBy { it.sumBy { it.width } }?.sumBy { it.width } ?: 0

    private fun findTotalStitchesDone(stitches: Array<Array<Stitch>>, currentRow: Int) =
            stitchesDoneInRow + stitches.sliceArray(IntRange(0, currentRow - 1)).sumBy { it.size }

    private fun findCurrentDistanceInRow(stitches: Array<Array<Stitch>>, currentRow: Int): Int {
        val rowDoneRange =
                if(currentRowDirection == 1)
                    IntRange(0, nextStitchInRow - 1)
                else
                    IntRange(nextStitchInRow + 1, stitches[currentRow].size-1)
        return stitches[currentRow].sliceArray(rowDoneRange).sumBy { it.width }
    }

    fun rowDirection(row: Int = currentRow) =
            if (oddRowsOpposite && (row % 2 == 1)) -1 else 1

    val currentRowDirection: Int
        get() = rowDirection(currentRow)

    private val isEndOfRow: Boolean
        get() = stitchesLeftInRow == 0

    val isStartOfRow: Boolean
        get() = stitchesDoneInRow == 0

    val stitchesLeftInRow: Int
        get() = stitches[currentRow].size - stitchesDoneInRow

    val nextStitchInRow: Int
        get() = if (currentRowDirection == 1) stitchesDoneInRow else (stitches[currentRow].size - 1 - stitchesDoneInRow)

    val numRows: Int
        get() = stitches.size

    val isFinished: Boolean
        get() = totalStitches == totalStitchesDone
}
