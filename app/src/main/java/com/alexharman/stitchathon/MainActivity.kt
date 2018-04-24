package com.alexharman.stitchathon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.database.AppDatabase
import com.alexharman.stitchathon.databaseAccessAsyncTasks.*
import kotlin.math.min

class MainActivity :
        AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        ImportImageDialog.ImportImageDialogListener,
        GoToStitchDialog.GoToStitchDialogListener,
        OpenPattern {

    private lateinit var stitchCount: TextView
    private lateinit var rowCount: TextView
    private lateinit var completeCount: TextView
    private lateinit var knitPatternView: KnitPatternView
    private lateinit var knitPatternViewGestureDetector: GestureDetectorCompat
    private var knitPatternDrawer: KnitPatternDrawer? = null
    private var importImageDialog: ImportImageDialog? = null
    private val preferenceListener = MySharedPreferenceListener()


    companion object {
        internal lateinit var db: AppDatabase
        const val READ_EXTERNAL_IMAGE = 42
        const val READ_EXTERNAL_JSON_PATTERN = 55
        const val OPEN_INTERNAL_PATTERN = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setUpUI()
        db = AppDatabase.getAppDatabase(applicationContext)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val patternName = sharedPreferences.getString(getString(R.string.current_pattern_name_key), null)
        if (patternName != null) {
            openPattern(patternName)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val zoomButton = menu.findItem(R.id.zoom_button)
        val lockButton = menu.findItem(R.id.lock_button)
        lockButton.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.lock_to_screen_key), false)
        lockButton.icon = getDrawable(if (lockButton.isChecked) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp )
        zoomButton.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.fit_pattern_width_key), false)
        zoomButton.icon.alpha = resources.getInteger(if (zoomButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        return true
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        when (id) {
            R.id.nav_open -> selectInternalPattern()
            R.id.nav_import_pattern -> selectExternalFile("application/json", READ_EXTERNAL_JSON_PATTERN)
            R.id.nav_import_image -> importImage()
            R.id.nav_about_app -> AppInfoDialog().show(supportFragmentManager, "App info")
            R.id.nav_settings -> openOptions()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPatternReturned(knitPattern: KnitPattern, knitPatternDrawer: KnitPatternDrawer, thumbnail: Bitmap) {
        setKnitPattern(knitPattern, knitPatternDrawer, thumbnail)
    }

    private fun setUpUI() {
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        toggle.syncState()
        drawer.addDrawerListener(toggle)

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)

        knitPatternView = findViewById(R.id.knitView)
        stitchCount = findViewById(R.id.stitch_counter)
        stitchCount.text = getString(R.string.stitches_done, 0)
        rowCount = findViewById(R.id.row_counter)
        rowCount.text = getString(R.string.rows_done, 0)
        completeCount = findViewById(R.id.complete_counter)
        completeCount.text = getString(R.string.complete_counter, 0)

        findViewById<Button>(R.id.increment_row_button).setOnClickListener { knitPatternDrawer?.incrementRow(); knitPatternView.updateCurrentView(); updateStitchCounter() }
        findViewById<Button>(R.id.undo_button).setOnClickListener { knitPatternDrawer?.undo(); knitPatternView.updateCurrentView(); updateStitchCounter() }
        knitPatternViewGestureDetector = GestureDetectorCompat(this, KnitPatternViewGestureListener())
        knitPatternView.setOnTouchListener { _, event -> knitPatternViewGestureDetector.onTouchEvent(event)}
    }

    private fun zoomButtonPressed(zoomButton: MenuItem) {
        zoomButton.isChecked = !zoomButton.isChecked
        zoomButton.icon.alpha = resources.getInteger(if (zoomButton.isChecked) R.integer.icon_alpha_selected else R.integer.icon_alpha_unselected)
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(R.string.fit_pattern_width_key), zoomButton.isChecked)
                .apply()
    }

    private fun gotToStitch() {
        val pattern = knitPatternDrawer?.knitPattern ?: return
        GoToStitchDialog.newInstance(pattern.currentRow, pattern.stitchesDoneInRow)
                .show(supportFragmentManager, "Go to stitch")
    }

    private fun lockButtonPressed(lockButton: MenuItem) {
        lockButton.isChecked = !lockButton.isChecked
        lockButton.icon = getDrawable(if (lockButton.isChecked) R.drawable.ic_lock_closed_white_24dp else R.drawable.ic_lock_open_white_24dp )
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(R.string.lock_to_screen_key), lockButton.isChecked)
                .apply()
    }

    private fun selectInternalPattern() {
        val intent = Intent(this, OpenPatternActivity::class.java)
        startActivityForResult(intent, OPEN_INTERNAL_PATTERN)
    }

    private fun openOptions() {
        val intent = Intent(this, AppOptionsActivity::class.java)
        startActivity(intent)
    }

    internal fun selectExternalFile(type: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        startActivityForResult(intent, requestCode)
    }

    fun updateStitchCounter() {
        val pattern = knitPatternDrawer?.knitPattern
        stitchCount.text = getString(R.string.stitches_done, pattern?.stitchesDoneInRow ?: 0)
        rowCount.text = getString(R.string.rows_done, pattern?.currentRow ?: 0)
        completeCount.text = getString(R.string.complete_counter, 100 * (pattern?.totalStitchesDone?:0) / (pattern?.totalStitches?:1))
    }

    override fun onPause() {
        super.onPause()
        if (knitPatternDrawer != null)
            savePattern()
    }

    private fun setKnitPattern(knitPattern: KnitPattern,
                               knitPatternDrawer: KnitPatternDrawer = KnitPatternDrawer(knitPattern, this),
                               thumbnail: Bitmap = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)) {
        this.knitPatternDrawer = knitPatternDrawer
        knitPatternView.setPattern(knitPatternDrawer)
        updateStitchCounter()
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        findViewById<TextView>(R.id.nav_drawer_pattern_name).text = knitPattern.name
        findViewById<ImageView>(R.id.nav_drawer_image).setImageBitmap(thumbnail)
        findViewById<Toolbar>(R.id.toolbar)?.title = knitPattern.name

        editor.putString(getString(R.string.current_pattern_name_key), knitPattern.name)
        editor.apply()
    }

    private fun clearKnitPattern() {
        this.knitPatternDrawer = null
        knitPatternView.clearPattern()
        findViewById<ImageView>(R.id.nav_drawer_image).setImageResource(R.drawable.logo)
        findViewById<TextView>(R.id.nav_drawer_pattern_name).text = ""
        findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.title_activity_main)
        updateStitchCounter()
        getPreferences(Context.MODE_PRIVATE).edit()
                .remove(getString(R.string.current_pattern_name_key))
                .apply()
    }

    private fun importImage() {
        importImageDialog = ImportImageDialog()
        importImageDialog?.show(supportFragmentManager, "Importing image")
    }

    private fun openPattern(patternName: String) {
        OpenPatternTask(this, this).execute(patternName)
    }

    private fun importJson(uri: Uri?) {
        ImportJsonTask(this, this).execute(uri)
    }

    private fun savePattern() {
        SavePatternChangesTask().execute(knitPatternDrawer?.knitPattern)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == READ_EXTERNAL_JSON_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                importJson(resultData.data)
            }
        }
        if (requestCode == READ_EXTERNAL_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultData != null && resultData.data != null) {
                importImageDialog!!.setUri(resultData.data!!)
            }
        }
        if (requestCode == OPEN_INTERNAL_PATTERN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                // TODO: use prefs instead of returning?
                openPattern(resultData.getStringExtra("patternName"))
            }
        }
    }

    override fun onImportImageDialogOK(imageUri: Uri, name: String, width: Int, height: Int, numColours: Int) {
        ImportImageTask(this, this, imageUri, name, width, height, numColours).execute()
    }

    override fun onGoToStitchReturn(row: Int, col: Int) {
        val knitPattern = knitPatternDrawer?.knitPattern ?: return
        val myRow = if (row < 0) knitPattern.currentRow else min(knitPattern.numRows - 1, row)
        val myCol = if (col < 0) min(knitPattern.stitchesDoneInRow, knitPattern.stitches[myRow].size - 1) else min(knitPattern.stitches[myRow].size - 1, col)
        knitPatternDrawer?.markStitchesTo(myRow, myCol)
        knitPatternView.updateCurrentView()
        updateStitchCounter()
    }

    private inner class MySharedPreferenceListener : OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == getString(R.string.current_pattern_name_key) && !sharedPreferences.contains(getString(R.string.current_pattern_name_key))) {
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
    }
}
