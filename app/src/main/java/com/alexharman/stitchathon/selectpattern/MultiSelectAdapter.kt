package com.alexharman.stitchathon.selectpattern

import androidx.recyclerview.widget.RecyclerView

abstract class MultiSelectAdapter<D>(private var dataset: MutableList<D>, private val listener: MultiSelectListener<D>) :
        RecyclerView.Adapter<SelectableViewHolder<D>>() {

    interface MultiSelectListener<D> {
        fun onSelectionStart()
        fun onSelectionEnd()
        fun onSingleItemSelected(item: D)
    }

    private val selected = mutableListOf<D>()
    private var recyclerView: RecyclerView? = null

    override fun onBindViewHolder(holder: SelectableViewHolder<D>, position: Int) {
        holder.bindData(dataset[position])
        holder.view.setOnClickListener { onItemClicked(holder) }
        holder.view.setOnLongClickListener { onItemLongPressed(holder); true }
        if (selected.contains(dataset[position])) holder.view.isActivated = true
    }

    override fun onViewRecycled(holder: SelectableViewHolder<D>) {
        super.onViewRecycled(holder)
        holder.view.isActivated = false
    }

    override fun getItemCount() = dataset.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

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

    fun removeItem(item: D) {
        val position = dataset.indexOf(item)
        if (position != -1) {
            dataset.removeAt(position)
            selected.remove(item)
            notifyItemRemoved(position)
        }
    }

    fun setDataset(dataset: MutableList<D>) {
        this.dataset = dataset
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<D> {
        return selected.toList()
    }

    fun deselectAll() {
        val recyclerView = this.recyclerView ?: return
        for (item in selected) {
            (recyclerView.findViewHolderForAdapterPosition(dataset.indexOf(item)) as SelectableViewHolder<*>)
                    .deselect()
        }
        selected.clear()
    }
}