package com.alexharman.stitchathon

interface BasePresenter<V> {
    var view: V

    fun resume() {}

    fun pause() {}
}