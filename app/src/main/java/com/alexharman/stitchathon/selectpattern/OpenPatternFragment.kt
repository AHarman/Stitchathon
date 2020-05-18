package com.alexharman.stitchathon.selectpattern

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.alexharman.stitchathon.MainActivity
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.repository.KnitPatternDataSource
import com.alexharman.stitchathon.repository.KnitPatternRepository

class OpenPatternFragment : Fragment(),
        KnitPatternDataSource.GetPatternInfoCallback,
        ActionMode.Callback,
        MultiSelectAdapter.MultiSelectListener<Pair<String, Bitmap?>> {

    private lateinit var recyclerView: RecyclerView
    private var patterns = mutableListOf<Pair<String, Bitmap?>>()
    private var viewAdapter = MyAdapter(patterns, this)
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var actionMode: android.support.v7.view.ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.pattern_select_grid)
        recyclerView.setHasFixedSize(true)
        viewManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = viewManager
        recyclerView.adapter = viewAdapter
    }

    override fun onPatternInfoReturn(result: Array<Pair<String, Bitmap?>>) {
        viewAdapter.setDataset(result.toMutableList())
        viewAdapter.notifyDataSetChanged()
    }

    override fun onGetKnitPatternInfoFail() {
        // Do nothing I suppose?
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = getString(R.string.open_pattern_title)
        KnitPatternRepository.getInstance(context ?: return).getKnitPatternNames(this)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        this.actionMode = mode
        mode?.menuInflater?.inflate(R.menu.delete_button, menu)
        mode?.setTitle(R.string.delete_patterns)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        viewAdapter.deselectAll()
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete_button) {
            deleteSelectedPatterns()
            mode?.finish()
        }
        return true
    }

    override fun onSelectionStart() {
        (activity as AppCompatActivity).startSupportActionMode(this)
    }

    override fun onSelectionEnd() {
        actionMode?.finish()
    }

    override fun onSingleItemSelected(item: Pair<String, Bitmap?>) {
        (activity as MainActivity?)?.openPattern(item.first)
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun deleteSelectedPatterns() {
        val patterns = viewAdapter.getSelectedItems().map { it.first }.toTypedArray()
        (activity as MainActivity?)?.deletePatterns(*patterns)
        viewAdapter.removeSelectedItems()
    }

    inner class MyAdapter(dataset: MutableList<Pair<String, Bitmap?>>, listener: MultiSelectListener<Pair<String, Bitmap?>>) :
            MultiSelectAdapter<Pair<String, Bitmap?>>(dataset, listener) {

        init {
            registerAdapterDataObserver(
                    object : RecyclerView.AdapterDataObserver() {
                        override fun onChanged() {
                            updateVisibility()
                        }
                    })
        }

        private fun updateVisibility() {
            recyclerView.visibility = if (itemCount == 0) View.GONE else View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableViewHolder<Pair<String, Bitmap?>> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
            return MyViewHolder(view)
        }
    }

    inner class MyViewHolder(itemView: View): SelectableViewHolder<Pair<String, Bitmap?>>(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.grid_item_text)
        private val thumbnailView: ImageView = itemView.findViewById(R.id.grid_item_image)

        override fun bindData(data: Pair<String, Bitmap?>) {
            nameTextView.text = data.first
            thumbnailView.setImageBitmap(data.second)
        }
    }
}
