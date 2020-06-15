package com.alexharman.stitchathon.selectpattern

import SelectPatternContract
import android.graphics.Bitmap
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class SelectPatternPresenter(
        override var view: SelectPatternContract.View,
        private val repository: KnitPatternDataSource
    ):
    SelectPatternContract.Presenter,
    KnitPatternDataSource.GetPatternInfoListener {

    init {
        view.presenter = this
    }

    override fun resume() {
        repository.getKnitPatternsInfo(this)
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
}