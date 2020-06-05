package com.alexharman.stitchathon.loading

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView

interface LoadingContract {
    interface View<P>: BaseView<P> {
        fun showLoadingBar()

        fun dismissLoadingBar()
    }

    interface Presenter<V>: BasePresenter<V> { }
}