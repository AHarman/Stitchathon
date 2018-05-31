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
    var stitchesDoneInRow = 0
        private set
    var totalStitchesDone = 0
        private set
    var currentRow = 0
        private set

    // Assuming doubleknit for now. Will be false for knitting on the round
    val oddRowsOpposite = true

    constructor(name: String, stitches: Array<Array<Stitch>>, currentRow: Int = 0, nextStitchInRow: Int = 0) {
        this.name = name
        this.stitches = stitches
        this.currentRow = currentRow
        this.stitchesDoneInRow = stitchesDoneInRow
        stitchTypes = buildStitchTypes(stitches)
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

    fun increment (numStitches: Int = 1) {
        for (i in 0 until numStitches) {
            if (isFinished)
                return

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

    private fun findTotalStitchesDone(stitches: Array<Array<Stitch>>, currentRow: Int): Int {
        var totalStitchesDone = stitchesDoneInRow
        if (currentRow > 0)
            totalStitchesDone += stitches.sliceArray(IntRange(0, currentRow - 1)).sumBy { it.size }
        return totalStitchesDone
    }

    private fun findCurrentDistanceInRow(stitches: Array<Array<Stitch>>, currentRow: Int): Int {
        val stitchesDone: Array<Stitch>
        if (rowDirection == 1)
            stitchesDone = stitches[currentRow].sliceArray(IntRange(0, nextStitchInRow - 1))
        else
            stitchesDone = stitches[currentRow].sliceArray(IntRange(nextStitchInRow + 1, stitches[currentRow].size-1))
        return stitchesDone.sumBy { it.width }
    }

    val rowDirection: Int
        get() = if (oddRowsOpposite && (currentRow % 2 == 1)) -1 else 1

    private val isEndOfRow: Boolean
        get() = stitchesLeftInRow == 0

    val isStartOfRow: Boolean
        get() = stitchesDoneInRow == 0

    val stitchesLeftInRow: Int
        get() = stitches[currentRow].size - stitchesDoneInRow

    val nextStitchInRow: Int
        get() = if (rowDirection == 1) stitchesDoneInRow else (stitches[currentRow].size - 1 - stitchesDoneInRow)

    val numRows: Int
        get() = stitches.size

    val isFinished: Boolean
        get() = totalStitches == totalStitchesDone
}
