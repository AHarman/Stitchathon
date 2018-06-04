package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask

class OpenPatternFragment : Fragment(),
        GetNamesAndImagesTask.GetNamesAndThumbnails,
        MultiSelectAdapter.MultiSelectListener<Pair<String, Bitmap>> {

    private lateinit var recyclerView: RecyclerView
    private var patterns = mutableListOf<Pair<String, Bitmap>>()
    private var viewAdaptor = MyAdapter(patterns, this)
    private var viewManager = GridLayoutManager(context, 3)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.pattern_select_grid)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = viewManager
        recyclerView.adapter = viewAdaptor
        GetNamesAndImagesTask(context ?: return, this).execute()
    }

    override fun onNamesAndThumbnailsReturn(result: Array<Pair<String, Bitmap>>) {
        viewAdaptor.setDataset(result.toMutableList())
        viewAdaptor.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = getString(R.string.open_pattern_title)
    }

    override fun onSelectionStart() {
        Log.d("Open", "onSelectionStart")
    }

    override fun onSelectionEnd() {
        Log.d("Open", "onSelectionEnd")
    }

    override fun onSingleItemSelected(item: Pair<String, Bitmap>) {
        Log.d("Open", "onSingleItemSelected")
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
