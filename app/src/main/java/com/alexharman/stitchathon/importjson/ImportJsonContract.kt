package com.alexharman.stitchathon.importjson

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.loading.LoadingContract

interface ImportJsonContract {
    interface View: BaseView<Presenter>, LoadingContract.View<Presenter> {
        fun patternImported(pattern: KnitPattern)
    }

    interface Presenter: BasePresenter<View>, LoadingContract.Presenter<View> {
        fun importJson(uri: String, name: String, oddRowsOpposite: Boolean)
    }
}