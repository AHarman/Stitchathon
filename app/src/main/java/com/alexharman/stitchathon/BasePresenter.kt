package com.alexharman.stitchathon

interface BasePresenter<V> {
    var view: V

    fun resume() {
        // Empty default method in case functionality is not required
    }

    fun pause() {
        // Empty default method in case functionality is not required
    }
}