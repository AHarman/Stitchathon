package com.alexharman.stitchathon.settings

import com.alexharman.stitchathon.BasePresenter
import com.alexharman.stitchathon.BaseView

interface SettingsContract {
    interface View: BaseView<Presenter> {    }

    interface Presenter: BasePresenter<View> {
        fun clearAllPreferences()
        fun deleteAllPatterns()
    }
}