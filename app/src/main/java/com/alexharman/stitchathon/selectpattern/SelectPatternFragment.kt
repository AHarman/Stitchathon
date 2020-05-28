package com.alexharman.stitchathon.selectpattern

import SelectPatternContract
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexharman.stitchathon.BaseFragmentView
import com.alexharman.stitchathon.R

// TODO: See if we can get a MutliSelectAdapter presenter/contract?
class SelectPatternFragment : BaseFragmentView<SelectPatternContract.View, SelectPatternContract.Presenter>(),
        SelectPatternContract.View,
        ActionMode.Callback,
        MultiSelectAdapter.MultiSelectListener<Pair<String, Bitmap?>> {

    override val view: SelectPatternFragment = this
    private lateinit var recyclerView: RecyclerView
    private var patterns = mutableListOf<Pair<String, Bitmap?>>()
    private var viewAdapter = MyAdapter(patterns, this)
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var actionMode: ActionMode? = null

    override lateinit var presenter: SelectPatternContract.Presenter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = getString(R.string.open_pattern_title)
    }

    override fun setAvailablePatterns(patterns: Array<Pair<String, Bitmap?>>) {
        viewAdapter.setDataset(patterns.toMutableList())
        viewAdapter.notifyDataSetChanged()
    }

    override fun removePatterns(patterns: Array<Pair<String, Bitmap?>>) {
        patterns.forEach {  viewAdapter.removeItem(it) }
        viewAdapter.notifyDataSetChanged()
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.delete_button, menu)
        mode?.setTitle(R.string.delete_patterns)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

    override fun onDestroyActionMode(mode: ActionMode?) {
        viewAdapter.deselectAll()
        actionMode = null
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete_button) {
            deleteSelectedPatterns()
            mode?.finish()
        }
        return true
    }

    override fun onSelectionStart() {
        actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
    }

    override fun onSelectionEnd() {
        actionMode?.finish()
    }

    override fun onSingleItemSelected(item: Pair<String, Bitmap?>) {
        presenter.selectPattern(item.first)
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun deleteSelectedPatterns() {
        val patterns = viewAdapter.getSelectedItems().map { it.first }.toTypedArray()
        presenter.deletePatterns(patterns)
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
