package com.alexharman.stitchathon

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class SelectableViewHolder<D>(val view: View): RecyclerView.ViewHolder(view) {

    abstract fun bindData(data: D)

    fun select() {
        view.isActivated = true
    }

    fun deselect() {
        view.isActivated = false
    }

    fun toggleSelected() {
        view.isActivated = !view.isActivated
    }
}