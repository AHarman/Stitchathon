package com.alexharman.stitchathon

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask

class OpenPatternFragment : Fragment(),
        GetNamesAndImagesTask.GetNamesAndThumbnails {

    private lateinit var recyclerView: RecyclerView
    private var patterns = emptyArray<Pair<String, Bitmap>>()
    private var viewAdaptor = MyAdapter(patterns)
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

    override fun onNamesAndThumbnailsReturn(map: Array<Pair<String, Bitmap>>) {
        viewAdaptor.dataset = map.toList().toTypedArray()
        viewAdaptor.notifyDataSetChanged()
    }

    class MyAdapter(var dataset: Array<Pair<String, Bitmap>>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val nameTextView: TextView = view.findViewById(R.id.grid_item_text)
            val thumbnailView: ImageView = view.findViewById(R.id.grid_item_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.nameTextView.text = dataset[position].first
            holder.thumbnailView.setImageBitmap(dataset[position].second)
        }

        override fun getItemCount() = dataset.size
    }
}
