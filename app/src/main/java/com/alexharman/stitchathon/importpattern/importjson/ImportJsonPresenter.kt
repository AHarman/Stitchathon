package com.alexharman.stitchathon.importpattern.importjson

import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.importpattern.BaseImportPatternPresenter
import com.alexharman.stitchathon.repository.KnitPatternDataSource

class ImportJsonPresenter(
        override var view: ImportJsonContract.View,
        private val repository: KnitPatternDataSource) :
        BaseImportPatternPresenter<ImportJsonContract.View>(),
        ImportJsonContract.Presenter,
        KnitPatternDataSource.ImportPatternListener {

    init {
        view.presenter = this
    }

    override fun importButtonPressed() {
        val uri = uri ?: return
        val name = name ?: return
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