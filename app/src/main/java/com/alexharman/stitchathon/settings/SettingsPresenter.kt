package com.alexharman.stitchathon.settings

import com.alexharman.stitchathon.repository.KnitPatternDataSource

class SettingsPresenter (
        override var view: SettingsContract.View,
        private val repository: KnitPatternDataSource
    ): SettingsContract.Presenter {

    init {
        view.presenter = this
    }

    override fun clearAllPreferences() {
        repository.clearPreferences()
    }

    override fun deleteAllPatterns() {
        repository.deleteAllKnitPatterns()
    }
}