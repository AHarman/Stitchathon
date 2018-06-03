package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.database.AppDatabase
import com.alexharman.stitchathon.databaseAccessAsyncTasks.OpenPattern
import com.alexharman.stitchathon.databaseAccessAsyncTasks.OpenPatternTask
import com.alexharman.stitchathon.databaseAccessAsyncTasks.SavePatternChangesTask
import kotlin.math.min

class KnitPatternFragment : Fragment(),
        GoToStitchDialog.GoToStitchDialogListener,
        OpenPattern {

    private var patternThumbnailView: ImageView? = null
    private var patternNameView: TextView? = null
    private var toolbar: Toolbar? = null
    private lateinit var stitchCount: TextView
    private lateinit var rowCount: TextView
    private lateinit var completeCount: TextView
    private lateinit var knitPatternView: KnitPatternView
    private lateinit var knitPatternViewGestureDetector: GestureDetectorCompat
    private var knitPatternDrawer: KnitPatternDrawer? = null
    private val preferenceListener = MySharedPreferenceListener()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val patternName = sharedPreferences.getString(PreferenceKeys.CURRENT_PATTERN_NAME, null)
        if (patternName != null) {
            openPattern(patternName)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_knitpattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        knitPatternView = view.findViewById(R.id.knitView)
        stitchCount = view.findViewById(R.id.stitch_counter)
        stitchCount.text = getString(R.string.stitches_done, 0)
        rowCount = view.findViewById(R.id.row_counter)
        rowCount.text = getString(R.string.rows_done, 0)
        completeCount = view.findViewById(R.id.complete_counter)
        completeCount.text = getString(R.string.complete_counter, 0)

        view.findViewById<Button>(R.id.increment_row_button).setOnClickListener { knitPatternDrawer?.incrementRow(); knitPatternView.updateCurrentView(); updateStitchCounter() }
        view.findViewById<Button>(R.id.undo_button).setOnClickListener { knitPatternDrawer?.undo(); knitPatternView.updateCurrentView(); updateStitchCounter() }
        knitPatternViewGestureDetector = GestureDetectorCompat(context, KnitPatternViewGestureListener())
        knitPatternView.setOnTouchListener { _, event -> knitPatternViewGestureDetector.onTouchEvent(event) }

        val knitPatternDrawer = this.knitPatternDrawer
        if (knitPatternDrawer != null)
            knitPatternView.knitPatternDrawer = knitPatternDrawer
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = this.activity ?: return
        setHasOptionsMenu(true)
        patternNameView = activity.findViewById(R.id.nav_drawer_pattern_name)
        patternThumbnailView = activity.findViewById(R.id.nav_drawer_image)
        toolbar = activity.findViewById(R.id.toolbar)
        toolbar?.title = knitPatternDrawer?.knitPattern?.name ?: getString(R.string.title_activity_main)
        db = AppDatabase.getAppDatabase(activity)
    }

    override fun onPause() {
        super.onPause()
        if (knitPatternDrawer != null)
            savePattern()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        val zoomButton = menu.findItem(R.id.zoom_button)
        val lockButton = menu.findItem(R.id.lock_button)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        lockButton.isChecked = prefs.getBoolean(PreferenceKeys.LOCK_TO_SCREEN, false)
        lockButton.icon = context?.getDrawable(if (lockButton.isChecked) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp )
        lockButton.icon.alpha = resources.getInteger(if (lockButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        zoomButton.isChecked = prefs.getBoolean(PreferenceKeys.FIT_PATTERN_WIDTH, false)
        zoomButton.icon.alpha = resources.getInteger(if (zoomButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_settings -> true
            R.id.zoom_button -> { zoomButtonPressed(item); true }
            R.id.go_to_stitch_button -> { gotToStitch(); true }
            R.id.lock_button -> { lockButtonPressed(item); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun lockButtonPressed(lockButton: MenuItem) {
        lockButton.isChecked = !lockButton.isChecked
        lockButton.icon = context?.getDrawable(if (lockButton.isChecked) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp )
        lockButton.icon.alpha = resources.getInteger(if (lockButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PreferenceKeys.LOCK_TO_SCREEN, lockButton.isChecked)
                .apply()
    }

    private fun zoomButtonPressed(zoomButton: MenuItem) {
        zoomButton.isChecked = !zoomButton.isChecked
        zoomButton.icon.alpha = resources.getInteger(if (zoomButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PreferenceKeys.FIT_PATTERN_WIDTH, zoomButton.isChecked)
                .apply()
    }

    private fun gotToStitch() {
        val pattern = knitPatternDrawer?.knitPattern ?: return
        GoToStitchDialog.newInstance(pattern.currentRow, pattern.stitchesDoneInRow)
                .show(childFragmentManager, "Go to stitch")
    }

    override fun onGoToStitchReturn(row: Int, col: Int) {
        val knitPattern = knitPatternDrawer?.knitPattern ?: return
        val myRow = if (row < 0) knitPattern.currentRow else min(knitPattern.numRows - 1, row)
        val myCol = if (col < 0) min(knitPattern.stitchesDoneInRow, knitPattern.stitches[myRow].size - 1) else min(knitPattern.stitches[myRow].size - 1, col)
        knitPatternDrawer?.markStitchesTo(myRow, myCol)
        knitPatternView.updateCurrentView()
        updateStitchCounter()
    }

    fun openPattern(patternName: String) {
        val context = this.context ?: return
        OpenPatternTask(context, this).execute(patternName)
    }

    override fun onPatternReturned(knitPattern: KnitPattern, knitPatternDrawer: KnitPatternDrawer, thumbnail: Bitmap) {
        setKnitPattern(knitPattern, knitPatternDrawer, thumbnail)
    }

    private fun setKnitPattern(knitPattern: KnitPattern,
                               knitPatternDrawer: KnitPatternDrawer = KnitPatternDrawer(knitPattern, PreferenceManager.getDefaultSharedPreferences(context)),
                               thumbnail: Bitmap = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)) {
        this.knitPatternDrawer = knitPatternDrawer
        knitPatternView.knitPatternDrawer = knitPatternDrawer
        updateStitchCounter()
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        patternNameView?.text = knitPattern.name
        patternThumbnailView?.setImageBitmap(thumbnail)
        toolbar?.title = knitPattern.name

        editor.putString(PreferenceKeys.CURRENT_PATTERN_NAME, knitPattern.name)
        editor.apply()
    }

    fun updateStitchCounter() {
        val pattern = knitPatternDrawer?.knitPattern ?: return
        stitchCount.text = getString(R.string.stitches_done, pattern.stitchesDoneInRow)
        rowCount.text = getString(R.string.rows_done, pattern.currentRow)
        completeCount.text = getString(R.string.complete_counter, 100 * pattern.totalStitchesDone / pattern.totalStitches)
    }

    private fun savePattern() {
        SavePatternChangesTask().execute(knitPatternDrawer?.knitPattern)
    }

    private fun clearKnitPattern() {
        this.knitPatternDrawer = null
        knitPatternView.clearPattern()
        patternThumbnailView?.setImageResource(R.drawable.logo)
        patternNameView?.text = ""
        toolbar?.title = getString(R.string.title_activity_main)
        updateStitchCounter()
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                .apply()
    }

    private inner class MySharedPreferenceListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == PreferenceKeys.CURRENT_PATTERN_NAME ) {
                if (sharedPreferences.contains(PreferenceKeys.CURRENT_PATTERN_NAME))
                    openPattern(sharedPreferences.getString(PreferenceKeys.CURRENT_PATTERN_NAME, null))
                else
                    clearKnitPattern()
            }
        }
    }

    private inner class KnitPatternViewGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            knitPatternDrawer?.increment()
            updateStitchCounter()
            knitPatternView.updateCurrentView()
            return true
        }

        // Double taps get counted twice this way, letting users "spam" the screen
        override fun onDoubleTap(e: MotionEvent): Boolean {
            return onSingleTapUp(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            knitPatternView.scroll(distanceX, distanceY)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            // TODO: Add vibrate
            knitPatternDrawer?.incrementBlock()
            updateStitchCounter()
            knitPatternView.updateCurrentView()
        }
    }
}