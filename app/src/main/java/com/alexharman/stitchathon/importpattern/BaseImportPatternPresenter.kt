package com.alexharman.stitchathon.importpattern

abstract class BaseImportPatternPresenter<V>: BaseImportPatternContract.Presenter<V> {
    override var uri: String? = null

    override var name: String? = null

    override var oddRowsOpposite: Boolean = true
}