package com.alexharman.stitchathon.importjson

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class ImportJsonPresenter(
        override var view: ImportJsonContract.View,
        private val repository: KnitPatternDataSource) :
        ImportJsonContract.Presenter,
        KnitPatternDataSource.ImportPatternListener {

    init {
        view.presenter = this
    }

    override fun importJson(uri: String, name: String, oddRowsOpposite: Boolean) {
        view.showLoadingBar()
        repository.importNewJsonPattern(uri, name, oddRowsOpposite, this)
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