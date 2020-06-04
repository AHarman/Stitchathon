package com.alexharman.stitchathon.pattern

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import com.alexharman.stitchathon.BaseFragmentView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternPreferences
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.loading.ProgressbarDialog

class KnitPatternFragment : BaseFragmentView<PatternContract.View, PatternContract.Presenter>(),
        PatternContract.View {

    override val view = this
    override lateinit var presenter: PatternContract.Presenter
    private var patternThumbnailView: ImageView? = null
    private var patternNameView: TextView? = null
    private var toolbar: Toolbar? = null
    private var progressbarDialog: ProgressbarDialog? = null
    private lateinit var menu: Menu
    private lateinit var stitchCount: TextView
    private lateinit var rowCount: TextView
    private lateinit var completeCount: TextView
    private lateinit var knitPatternView: KnitPatternDisplayView
    private lateinit var knitPatternViewGestureDetector: GestureDetectorCompat

    private var pattern: KnitPattern? = null
    private var patternPreferences: KnitPatternPreferences? = null

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

        view.findViewById<Button>(R.id.increment_row_button).setOnClickListener { presenter.incrementRow() }
        view.findViewById<Button>(R.id.undo_button).setOnClickListener { presenter.undo() }
        knitPatternViewGestureDetector = GestureDetectorCompat(context, KnitPatternViewGestureListener())
        knitPatternView.setOnTouchListener { _, event -> knitPatternViewGestureDetector.onTouchEvent(event) }
        knitPatternView.setPattern(pattern, patternPreferences)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = this.activity ?: return
        setHasOptionsMenu(true)
        patternNameView = activity.findViewById(R.id.nav_drawer_pattern_name)
        patternThumbnailView = activity.findViewById(R.id.nav_drawer_image)
        toolbar = activity.findViewById(R.id.toolbar)
        toolbar?.title = pattern?.name ?: getString(R.string.title_activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        this.menu = menu

        val zoomButton = menu.findItem(R.id.zoom_button)
        val lockButton = menu.findItem(R.id.lock_button)
        lockButton.isChecked = presenter.lockToCurrentStitch
        lockButton.icon = context?.getDrawable(if (presenter.lockToCurrentStitch) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp)
        lockButton.icon.alpha = resources.getInteger(if (presenter.lockToCurrentStitch) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        zoomButton.icon.alpha = resources.getInteger(if (presenter.fitPatternWidth) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_settings -> true
            R.id.zoom_button -> { fitToPatternWidthButtonPressed(); true }
            R.id.go_to_stitch_button -> { gotToStitch(); true }
            R.id.lock_button -> { lockButtonPressed(item); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun patternUpdated() {
        this.knitPatternView.invalidate()
        updateProgressCounters()
    }

    override fun setPattern(pattern: KnitPattern?, patternPreferences: KnitPatternPreferences?) {
        this.pattern = pattern
        this.patternPreferences = patternPreferences
        knitPatternView.setPattern(pattern, patternPreferences)
        patternUpdated()
        patternNameView?.text = pattern?.name
//        patternThumbnailView?.setImageBitmap(thumbnail)
        toolbar?.title = pattern?.name ?: getString(R.string.title_activity_main)
    }

    override fun showLoadingBar() {
        if (progressbarDialog == null) {
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_load_title), getString(R.string.progress_bar_loading_pattern))
            progressbarDialog?.show(parentFragmentManager, "Progress dialog")
        }
    }

    override fun dismissLoadingBar() {
        progressbarDialog?.dismiss()
        progressbarDialog = null
    }

    override fun scrollToStitch(row: Int, col: Int) {
        knitPatternView.scrollToStitch(row, col)
    }

    private fun lockButtonPressed(lockButton: MenuItem) {
        presenter.lockToCurrentStitch = !presenter.lockToCurrentStitch
        lockButton.isChecked = presenter.lockToCurrentStitch
        lockButton.icon = context?.getDrawable(if (presenter.lockToCurrentStitch) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp)
        lockButton.icon.alpha = resources.getInteger(if (presenter.lockToCurrentStitch) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
    }

    private fun fitToPatternWidthButtonPressed() {
        presenter.fitPatternWidth = !presenter.fitPatternWidth
        val zoomButton = menu.findItem(R.id.zoom_button)
        zoomButton.isChecked = presenter.fitPatternWidth
        zoomButton.icon.alpha = resources.getInteger(if (presenter.fitPatternWidth) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        if (presenter.fitPatternWidth)
            knitPatternView.setZoomToPatternWidth()
        else
            knitPatternView.setZoom(1F)
    }

    private fun gotToStitch() {
        val pattern = pattern ?: return
        GoToStitchDialog.newInstance(pattern.currentRow, pattern.stitchesDoneInRow)
                .show(childFragmentManager, "Go to stitch")
    }

    private fun updateProgressCounters() {
        val percentage = 100 * (pattern?.totalStitchesDone ?: 0) / (pattern?.totalStitches ?: 1)
        stitchCount.text = getString(R.string.stitches_done, pattern?.stitchesDoneInRow ?: 0)
        rowCount.text = getString(R.string.rows_done, pattern?.currentRow ?: 0)
        completeCount.text = getString(R.string.complete_counter, percentage)
    }

    private inner class KnitPatternViewGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            presenter.increment()
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
            presenter.incrementBlock()
        }
    }
}