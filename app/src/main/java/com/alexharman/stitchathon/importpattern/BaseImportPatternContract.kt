package com.alexharman.stitchathon.importpattern

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.loading.LoadingContract
import com.alexharman.stitchathon.repository.KnitPatternDataSource

interface BaseImportPatternContract {
    interface View<P>: BaseView<P>, LoadingContract.View<P> {
        fun patternImported(pattern: KnitPattern)
    }

    interface Presenter<V>:
            BasePresenter<V>,
            LoadingContract.Presenter<V>,
            KnitPatternDataSource.ImportPatternListener {
        var uri: String?
        var name: String?
        var oddRowsOpposite: Boolean

        fun importButtonPressed()
    }
}