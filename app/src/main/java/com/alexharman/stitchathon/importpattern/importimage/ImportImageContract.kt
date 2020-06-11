package com.alexharman.stitchathon.importpattern.importimage

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.importpattern.BaseImportPatternContract
import com.alexharman.stitchathon.loading.LoadingContract

interface ImportImageContract {
    interface View:
            BaseImportPatternContract.View<Presenter>,
            LoadingContract.View<Presenter>,
            BaseView<Presenter> {

    }

    interface Presenter:
            BaseImportPatternContract.Presenter<View>,
            LoadingContract.Presenter<View>,
            BasePresenter<View> {

        var stitchesWide: Int?
        var stitchesHigh: Int?
        var numColours: Int?
    }
}