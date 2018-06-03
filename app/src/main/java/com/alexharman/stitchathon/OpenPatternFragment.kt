package com.alexharman.stitchathon

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.alexharman.stitchathon.databaseAccessAsyncTasks.DeletePatternsTask
import com.alexharman.stitchathon.databaseAccessAsyncTasks.GetNamesAndImagesTask
import java.util.*

class OpenPatternFragment : Fragment(),
        GetNamesAndImagesTask.GetNamesAndThumbnails {

    lateinit var gridView: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this.context ?: return
        GetNamesAndImagesTask(context, this).execute()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpGridView(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = getString(R.string.open_pattern_title)
    }

    private fun setUpGridView(view: View) {
        gridView = view.findViewById(R.id.pattern_select_grid)
        gridView.adapter = MyAdaptor()
        gridView.emptyView = view.findViewById(R.id.empty_view)

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PreferenceKeys.CURRENT_PATTERN_NAME, gridView.adapter.getItem(position) as String)
                    .apply()
            activity?.supportFragmentManager?.popBackStack()
        }

        gridView.setMultiChoiceModeListener(MyMultiChoiceModeListener())
    }

    override fun onNamesAndThumbnailsReturn(map: HashMap<String, Bitmap>) {
        (gridView.adapter as MyAdaptor).addItems(map)
    }

    inner class MyMultiChoiceModeListener: AbsListView.MultiChoiceModeListener {
        override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
            gridView.getChildAt(position).isActivated = checked
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.delete_button, menu)
            mode.setTitle(R.string.select_patterns)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val context = this@OpenPatternFragment.context ?: return false
            if (item.itemId == R.id.delete_button) {
                val adaptor = gridView.adapter as MyAdaptor
                val toBeDeleted = ArrayList<String>()
                for (i in adaptor.count downTo 0) {
                    if (gridView.isItemChecked(i)) {
                        toBeDeleted.add(adaptor.getItem(i))
                        adaptor.removeItem(i)
                    }
                }
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                if (toBeDeleted.contains(prefs.getString(PreferenceKeys.CURRENT_PATTERN_NAME, ""))) {
                    prefs.edit().remove(PreferenceKeys.CURRENT_PATTERN_NAME).apply()
                }

                DeletePatternsTask(context).execute(*toBeDeleted.toTypedArray())
                mode.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {}
    }

    private inner class MyAdaptor : BaseAdapter() {
        private val patternNames = ArrayList<String>()
        private val bitmaps = ArrayList<Bitmap>()

        override fun getCount(): Int {
            return patternNames.size
        }

        override fun getItem(position: Int): String {
            return patternNames[position]
        }

        // Don't use this, the items don't have numerical IDs
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = this@OpenPatternFragment.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val gridItem: View

            if (convertView == null) {
                gridItem = inflater.inflate(R.layout.grid_item, null)
            } else {
                gridItem = convertView
            }
            val imageView = gridItem.findViewById<ImageView>(R.id.grid_item_image)
            imageView.setImageBitmap(bitmaps[position])
            val textView = gridItem.findViewById<TextView>(R.id.grid_item_text)
            textView.text = patternNames[position]

            return gridItem
        }

        internal fun removeItem(position: Int) {
            patternNames.removeAt(position)
            bitmaps.removeAt(position)
            notifyDataSetChanged()
        }

        internal fun addItems(thumbs: HashMap<String, Bitmap>) {
            for ((key, value) in thumbs) {
                patternNames.add(key)
                bitmaps.add(value)
            }
            notifyDataSetChanged()
        }
    }
}
