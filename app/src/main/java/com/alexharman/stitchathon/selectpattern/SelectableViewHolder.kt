package com.alexharman.stitchathon.selectpattern

import android.view.View
import androidx.recyclerview.widget.RecyclerView

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