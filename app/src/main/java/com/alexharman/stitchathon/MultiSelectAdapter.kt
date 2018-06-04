package com.alexharman.stitchathon

import android.support.v7.widget.RecyclerView

abstract class MultiSelectAdapter<D>(private var dataset: MutableList<D>, private val listener: MultiSelectListener<D>) :
        RecyclerView.Adapter<SelectableViewHolder<D>>() {

    interface MultiSelectListener<D> {
        fun onSelectionStart()
        fun onSelectionEnd()
        fun onSingleItemSelected(item: D)
    }

    private val selected = mutableListOf<D>()

    override fun onBindViewHolder(holder: SelectableViewHolder<D>, position: Int) {
        holder.bindData(dataset[position])
        holder.view.setOnClickListener { onItemClicked(holder) }
        holder.view.setOnLongClickListener { onItemLongPressed(holder); true }
    }

    override fun getItemCount() = dataset.size

    private fun onItemClicked(item: SelectableViewHolder<D>) {
        val data = dataset[item.adapterPosition]
        if (selected.size == 0) {
            listener.onSingleItemSelected(data)
        } else if (selected.contains(data)){
            selected.remove(data)
            item.view.isActivated = false
            if (selected.size == 0) {
                listener.onSelectionEnd()
            }
        } else {
            selected.add(data)
            item.view.isActivated = true
        }
    }

    private fun onItemLongPressed(item: SelectableViewHolder<D>) {
        if (selected.size == 0) {
            listener.onSelectionStart()
            selected.add(dataset[item.adapterPosition])
            item.view.isActivated = true
        } else {
            onItemClicked(item)
        }
    }

    fun addItem(item: D, position: Int) {
        dataset.add(position, item)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        dataset.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setDataset(dataset: MutableList<D>) {
        this.dataset = dataset
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<D> {
        return selected.toList()
    }
}