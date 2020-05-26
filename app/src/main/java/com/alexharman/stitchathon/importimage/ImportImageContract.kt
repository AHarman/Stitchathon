package com.alexharman.stitchathon.importimage

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern

interface ImportImageContract {
    interface View: BaseView<Presenter> {
        fun showLoadingBar()

        fun dismissLoadingBar()

        fun patternImported(pattern: KnitPattern)
    }

    interface Presenter: BasePresenter<View> {
        fun importImage(uri: String, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int)
    }
}