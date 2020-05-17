package com.alexharman.stitchathon.pattern

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import java.util.*

// TODO: Create shared preferences repo that uses shared preferences, pass in repo here.
class PatternPresenter(private val view: PatternContract.View, knitPattern: KnitPattern): PatternContract.Presenter {

    var pattern: KnitPattern = knitPattern
        set(value) {
            field = value;
            view.setPattern(value)
        }

    private val undoStack = Stack<Int>()

    init {
        view.presenter = this
    }

    override fun start() {
        view.setPattern(pattern)
    }

    override fun increment() {
        increment(1)
    }

    private fun increment(numStitches: Int) {
        undoStack.push(numStitches)
        pattern.increment(numStitches)
        view.patternUpdated()
    }

    override fun incrementBlock() {
        val stitchType = pattern.stitches[pattern.currentRow][pattern.nextStitchInRow].type
        val currentRow = pattern.stitches[pattern.currentRow]
        var stitchesToDo = 0
        while (stitchesToDo + pattern.stitchesDoneInRow < currentRow.size &&
                currentRow[pattern.nextStitchInRow + stitchesToDo * pattern.currentRowDirection].type == stitchType) {
            stitchesToDo++
        }
        increment(stitchesToDo)
    }

    override fun incrementRow() {
        increment(pattern.stitchesLeftInRow)
    }

    override fun goTo(row: Int, col: Int) {
        while (row > pattern.currentRow) {
            increment(pattern.stitchesLeftInRow)
        }
        while (row < pattern.currentRow) {
            undoRow()
        }
        while (col > pattern.stitchesDoneInRow) {
            increment()
        }
        while (col < pattern.stitchesDoneInRow) {
            pattern.undoStitch()
        }
        undoStack.clear()
    }

    override fun undo() {
        if (undoStack.size == 0) {
            undoRow()
        } else {
            for (i in 0 until undoStack.pop()) {
                pattern.undoStitch()
            }
        }
        view.patternUpdated()
    }

    private fun undoRow() {
        do {
            // TODO: This method should just be increment(-1) on KnitPattern
            pattern.undoStitch()
        } while (!pattern.isStartOfRow)
    }
}