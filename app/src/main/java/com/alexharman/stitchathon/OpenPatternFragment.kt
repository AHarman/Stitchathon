package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.view.ActionMode
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.alexharman.stitchathon.databaseAccessAsyncTasks.DeletePatternsTask
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask

class OpenPatternFragment : Fragment(),
        GetNamesAndImagesTask.GetNamesAndThumbnails,
        ActionMode.Callback,
        MultiSelectAdapter.MultiSelectListener<Pair<String, Bitmap>> {

    private lateinit var recyclerView: RecyclerView
    private var patterns = mutableListOf<Pair<String, Bitmap>>()
    private var viewAdapter = MyAdapter(patterns, this)
    private var viewManager = GridLayoutManager(context, 3)
    private var actionMode: android.support.v7.view.ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.pattern_select_grid)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = viewManager
        recyclerView.adapter = viewAdapter
        GetNamesAndImagesTask(context ?: return, this).execute()
    }

    override fun onNamesAndThumbnailsReturn(result: Array<Pair<String, Bitmap>>) {
        viewAdapter.setDataset(result.toMutableList())
        viewAdapter.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = getString(R.string.open_pattern_title)
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

    override fun onSingleItemSelected(item: Pair<String, Bitmap>) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PreferenceKeys.CURRENT_PATTERN_NAME, item.first)
                .apply()
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun deleteSelectedPatterns() {
        val context = this.context ?: return
        val patterns = viewAdapter.getSelectedItems().map { it.first }.toTypedArray()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        DeletePatternsTask(context).execute(*patterns)
        if (prefs.getString(PreferenceKeys.CURRENT_PATTERN_NAME, "") in patterns) {
            prefs
                    .edit()
                    .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                    .apply()
        }
        viewAdapter.removeSelectedItems()
    }

    inner class MyAdapter(dataset: MutableList<Pair<String, Bitmap>>, listener: MultiSelectListener<Pair<String, Bitmap>>) :
            MultiSelectAdapter<Pair<String, Bitmap>>(dataset, listener) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableViewHolder<Pair<String, Bitmap>> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
            return MyViewHolder(view)
        }
    }

    inner class MyViewHolder(itemView: View): SelectableViewHolder<Pair<String, Bitmap>>(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.grid_item_text)
        private val thumbnailView: ImageView = itemView.findViewById(R.id.grid_item_image)

        override fun bindData(data: Pair<String, Bitmap>) {
            nameTextView.text = data.first
            thumbnailView.setImageBitmap(data.second)
        }
    }
}
