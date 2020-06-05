package com.alexharman.stitchathon.importimage

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.loading.LoadingContract

interface ImportImageContract {
    interface View: BaseView<Presenter>, LoadingContract.View<Presenter> {
        fun patternImported(pattern: KnitPattern)
    }

    interface Presenter: BasePresenter<View>, LoadingContract.Presenter<View> {
        fun importImage(uri: String, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int)
    }
}