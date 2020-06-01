package com.alexharman.stitchathon.pattern

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern

interface PatternContract {
    interface View: BaseView<Presenter> {
        fun patternUpdated()

        fun setPattern(pattern: KnitPattern?)

        fun showLoadingBar()

        fun dismissLoadingBar()
    }

    interface Presenter: BasePresenter<View> {
        var fitPatternWidth: Boolean
        var lockToCurrentStitch: Boolean

        fun increment()

        fun incrementRow()

        fun incrementBlock()

        fun goTo(row: Int, col: Int)

        fun undo()
    }
}