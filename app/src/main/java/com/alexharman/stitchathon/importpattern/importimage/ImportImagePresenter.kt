package com.alexharman.stitchathon.importpattern.importimage

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.importpattern.BaseImportPatternPresenter
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class ImportImagePresenter(
        override var view: ImportImageContract.View,
        private val repository: KnitPatternDataSource):
        BaseImportPatternPresenter<ImportImageContract.View>(),
        ImportImageContract.Presenter,
        KnitPatternDataSource.ImportPatternListener {

    override var stitchesWide: Int? = null
    override var stitchesHigh: Int? = null
    override var numColours: Int? = null

    init {
        view.presenter = this
    }

    override fun importButtonPressed() {
        val uri = uri ?: return
        val name = name ?: return
        val stitchesHigh = stitchesHigh ?: return
        val stitchesWide = stitchesWide ?: return
        val numColours = numColours ?: return
        view.showLoadingBar()
        repository.importNewBitmapPattern(uri, name, stitchesWide, stitchesHigh, oddRowsOpposite, numColours, this)
    }


    override fun onPatternImport(pattern: KnitPattern) {
        repository.setCurrentKnitPattern(pattern.name)
        view.dismissLoadingBar()
        view.patternImported(pattern)
    }

    override fun onPatternImportFail() {
        view.dismissLoadingBar()
    }
}