package com.alexharman.stitchathon.selectpattern

import SelectPatternContract
import android.graphics.Bitmap
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class SelectPatternPresenter(
        override var view: SelectPatternContract.View,
        private val repository: KnitPatternDataSource
    ):
    SelectPatternContract.Presenter,
    KnitPatternDataSource.GetPatternInfoListener, KnitPatternDataSource.ImportPatternListener {

    init {
        view.presenter = this
    }

    override fun resume() {
        repository.getKnitPatternsInfo(this)
        repository.registerPatternImportedListener(this)
    }

    override fun pause() {
        repository.deregisterPatternImportedListener(this)
    }

    override fun selectPattern(patternName: String) {
        repository.setCurrentKnitPattern(patternName)
    }

    override fun deletePatterns(patternNames: Collection<String>) {
        repository.deleteKnitPatterns(patternNames)
        view.removePatterns(patternNames)
    }

    override fun onPatternInfoReturn(result: Array<Pair<String, Bitmap?>>) =
            view.setAvailablePatterns(result)

    override fun onGetKnitPatternInfoFail() {
        TODO("Not yet implemented")
    }

    // TODO: When refactoring the repository, it might be best for this to return the pattern info rather than the pattern
    override fun onPatternImport(pattern: KnitPattern) {
        repository.getKnitPatternsInfo(this)
    }

    override fun onPatternImportFail() {
        // No need to do anything.
    }
}