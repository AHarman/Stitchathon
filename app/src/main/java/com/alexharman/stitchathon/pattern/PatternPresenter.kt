package com.alexharman.stitchathon.pattern

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import java.util.*

class PatternPresenter(override var view: PatternContract.View, private val repository: KnitPatternDataSource):
        PatternContract.Presenter,
        KnitPatternDataSource.OpenPatternListener,
        KnitPatternDataSource.CurrentPatternListener {

    private var pattern: KnitPattern? = null
    private val undoStack = Stack<Int>()
    override var fitPatternWidth = false
    override var lockToCurrentStitch = false

    init {
        view.presenter = this
    }

    override fun resume() {
        repository.registerCurrentPatternListener(this)
        val currentPatternName = repository.getCurrentPatternName()
        if ((pattern == null && currentPatternName != null) || currentPatternName != pattern?.name) {
            openCurrentPattern()
        }
    }

    override fun pause() {
        repository.deregisterCurrentPatternListener(this)
        savePattern()
    }

    override fun onKnitPatternOpened(pattern: KnitPattern) {
        this.pattern = pattern
        view.setPattern(pattern, repository.getPatternPreferences(pattern.name))
        view.dismissLoadingBar()
    }

    override fun onOpenKnitPatternFail() {
        pattern = null
        view.setPattern(null, null)
        view.dismissLoadingBar()
    }

    override fun onCurrentPatternChanged(patternName: String) {
        openCurrentPattern()
    }

    override fun increment() {
        increment(1)
    }

    private fun increment(numStitches: Int) {
        undoStack.push(numStitches)
        pattern?.increment(numStitches)
        view.patternUpdated()
    }

    override fun incrementBlock() {
        val pattern = pattern ?: return
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
        val pattern = pattern ?: return
        increment(pattern.stitchesLeftInRow)
    }

    override fun goTo(row: Int, col: Int) {
        val pattern = pattern ?: return
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
        val pattern = pattern ?: return
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
        val pattern = pattern ?: return
        do {
            // TODO: This method should just be increment(-1) on KnitPattern
            pattern.undoStitch()
        } while (!pattern.isStartOfRow)
    }

    private fun savePattern() {
        val pattern = this.pattern ?: return
        repository.saveKnitPatternChanges(pattern)
    }

    private fun openCurrentPattern() {
        val patternName =  repository.getCurrentPatternName() ?: return
        view.showLoadingBar()
        repository.openKnitPattern(patternName, this)
    }
}