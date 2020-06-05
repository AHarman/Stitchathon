package com.alexharman.stitchathon.pattern

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences
import com.alexharman.stitchathon.loading.LoadingContract

interface PatternContract {
    interface View: LoadingContract.View<Presenter>, BaseView<Presenter> {
        fun patternUpdated()

        fun scrollToStitch(row: Int, col: Int)

        fun zoomToPatternWidth()

        fun resetZoom()

        fun setPattern(pattern: KnitPattern?, patternPreferences: KnitPatternPreferences?)
    }

    interface Presenter: LoadingContract.Presenter<View>, BasePresenter<View> {
        var fitPatternWidth: Boolean
        var lockToCurrentStitch: Boolean

        fun increment()

        fun incrementRow()

        fun incrementBlock()

        fun goTo(row: Int, col: Int)

        fun undo()
    }
}