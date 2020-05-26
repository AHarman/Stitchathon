package com.alexharman.stitchathon.importimage

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class ImportImagePresenter(
        override var view: ImportImageContract.View,
        private val repository: KnitPatternDataSource) : ImportImageContract.Presenter, KnitPatternDataSource.ImportPatternListener {

    init {
        view.presenter = this
    }

    override fun importImage(uri: String, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int) {
        repository.importNewBitmapPattern(uri, name, width, height, oddRowsOpposite, numColours, this)
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