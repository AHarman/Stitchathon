package com.alexharman.stitchathon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.KnitPackage.KnitPatternParser
import com.alexharman.stitchathon.database.AppDatabase
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference

class MainActivity :
        AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        ImportImageDialog.ImportImageDialogListener {

    private lateinit var stitchCount: TextView
    private lateinit var rowCount: TextView
    private lateinit var completeCount: TextView
    private lateinit var patternView: KnitPatternView
    private var knitPattern: KnitPattern? = null
    private var importImageDialog: ImportImageDialog? = null
    private val preferenceListener = MySharedPreferenceListener()

    companion object {
        private lateinit var db: AppDatabase
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
        val patternName = sharedPreferences.getString("pattern", null)
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

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

        patternView = findViewById(R.id.knitView)
        stitchCount = findViewById(R.id.stitch_counter)
        rowCount = findViewById(R.id.row_counter)
        completeCount = findViewById(R.id.complete_counter)

        findViewById<Button>(R.id.increment_row_button).setOnClickListener { patternView.incrementRow() }
        findViewById<Button>(R.id.undo_button).setOnClickListener { patternView.undo() }
    }

    private fun selectInternalPattern() {
        val intent = Intent(this, OpenPattern::class.java)
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
        if (knitPattern == null) {
            stitchCount.text = ""
            rowCount.text = ""
            completeCount.text = ""
            return
        }
        var s = getString(R.string.stitch_counter) + knitPattern!!.stitchesDoneInRow
        stitchCount.text = s
        s = getString(R.string.row_counter) + (knitPattern!!.currentRow + 1)
        rowCount.text = s
        s = getString(R.string.complete_counter) + 100 * knitPattern!!.totalStitchesDone / knitPattern!!.totalStitches + "%"
        completeCount.text = s
    }

    override fun onPause() {
        super.onPause()
        if (knitPattern != null)
            savePattern()
    }

    private fun setKnitPattern(knitPattern: KnitPattern,
                               knitPatternDrawer: KnitPatternDrawer = KnitPatternDrawer(knitPattern, this),
                               thumbnail: Bitmap = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)) {
        this.knitPattern = knitPattern
        patternView.setPattern(knitPatternDrawer)
        updateStitchCounter()
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        findViewById<TextView>(R.id.nav_drawer_pattern_name).setText(knitPattern.name)
        findViewById<ImageView>(R.id.nav_drawer_image).setImageBitmap(thumbnail)

        editor.putString("pattern", knitPattern.name)
        editor.apply()
    }

    private fun clearKnitPattern() {
        this.knitPattern = null
        patternView.clearPattern()
        findViewById<ImageView>(R.id.nav_drawer_image).setImageResource(R.drawable.logo)
        findViewById<TextView>(R.id.nav_drawer_pattern_name).setText("")
        updateStitchCounter()
        getPreferences(Context.MODE_PRIVATE).edit()
                .remove("pattern")
                .apply()
    }

    private fun importImage() {
        importImageDialog = ImportImageDialog()
        importImageDialog?.show(supportFragmentManager, "Importing image")
    }

    private fun openPattern(patternName: String) {
        OpenPatternTask(this).execute(patternName)
    }

    private fun importJson(uri: Uri?) {
        ImportJsonTask(this).execute(uri)
    }

    private fun savePattern() {
        SavePatternChangesTask().execute(knitPattern)
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
                openPattern(resultData.getStringExtra("patternName"))
            }
        }
    }

    override fun onImportImageDialogOK(imageUri: Uri, name: String, width: Int, height: Int, numColours: Int) {
        ImportImageTask(this, imageUri, name, width, height, numColours).execute()
    }

    private class SavePatternChangesTask : AsyncTask<KnitPattern, Void, Void>() {
        override fun doInBackground(vararg knitPatterns: KnitPattern): Void? {
            db.knitPatternDao().savePatternChanges(knitPatterns[0])
            return null
        }
    }

    private class OpenPatternTask(context: MainActivity) : AsyncTask<String, String, KnitPattern>() {
        private var progressbarDialog = ProgressbarDialog.newInstance(context.getString(R.string.progress_dialog_load_title), true, context.getString(R.string.progress_bar_loading_pattern))
        private lateinit var knitPatternDrawer: KnitPatternDrawer
        private lateinit var thumbnail: Bitmap
        private val context: WeakReference<MainActivity> = WeakReference(context)

        override fun onPreExecute() {
            progressbarDialog.show(context.get()!!.supportFragmentManager, "Opening")
        }

        override fun doInBackground(vararg strings: String): KnitPattern {
            val knitPattern = db.knitPatternDao().getKnitPattern(strings[0], context.get()!!)
            thumbnail = db.knitPatternDao().getThumbnail(context.get()!!, knitPattern.name)
            publishProgress(context.get()!!.getString(R.string.progress_bar_creating_bitmap))
            knitPatternDrawer = KnitPatternDrawer(knitPattern, context.get()!!)
            return knitPattern
        }

        override fun onProgressUpdate(vararg values: String) {
            progressbarDialog.updateText(values[0])
        }

        override fun onPostExecute(knitPattern: KnitPattern) {
            super.onPostExecute(knitPattern)
            context.get()!!.setKnitPattern(knitPattern, knitPatternDrawer, thumbnail)
            progressbarDialog.dismiss()
        }
    }

    private class ImportJsonTask(context: MainActivity) : ImportPatternTask<Uri>(context) {

        override fun doInBackground(vararg uris: Uri): KnitPattern? {
            var knitPattern: KnitPattern? = null
            try {
                knitPattern = KnitPatternParser.createKnitPattern(readTextFile(uris[0]))
                saveNewPattern(knitPattern)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return knitPattern
        }

        private fun readTextFile(uri: Uri): String {
            val stringBuilder = StringBuilder()
            try {
                val inputStream = context.get()!!.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String
                while (true) {
                    line = reader.readLine() ?: break
                    stringBuilder.append(line)
                }
                inputStream.close()
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return stringBuilder.toString()
        }
    }

    private class ImportImageTask(context: MainActivity,
                                  private val imageUri: Uri,
                                  private val patternName: String,
                                  private val width: Int,
                                  private val height: Int,
                                  private val numColours: Int) : ImportPatternTask<Void>(context) {

        private lateinit var sourceImg: Bitmap

        override fun doInBackground(vararg voids: Void): KnitPattern {
            sourceImg = readImageFile(imageUri)!!
            val knitPattern = ImageReader().readImage(sourceImg, patternName, width, height, numColours)
            saveNewPattern(knitPattern)
            return knitPattern
        }

        private fun readImageFile(uri: Uri): Bitmap? {
            var bitmap: Bitmap? = null
            val opts = BitmapFactory.Options()
            opts.inMutable = true
            try {
                val inputStream = context.get()!!.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }
    }

    private abstract class ImportPatternTask<V> internal constructor(context: MainActivity) : AsyncTask<V, String, KnitPattern>() {
        private var progressbarDialog = ProgressbarDialog.newInstance(context.getString(R.string.progress_dialog_import_title), true, context.getString(R.string.progress_bar_importing_pattern))
        internal var context: WeakReference<MainActivity> = WeakReference(context)
        private lateinit var knitPatternDrawer: KnitPatternDrawer
        private lateinit var thumbnail: Bitmap

        override fun onPreExecute() {
            progressbarDialog.show(context.get()!!.supportFragmentManager, "Importing image")
        }

        internal fun saveNewPattern(knitPattern: KnitPattern) {
            publishProgress(context.get()!!.getString(R.string.progress_bar_creating_bitmap))
            knitPatternDrawer = KnitPatternDrawer(knitPattern, context.get()!!)
            thumbnail = ThumbnailUtils.extractThumbnail(knitPatternDrawer.patternBitmap, 200, 200)
            publishProgress(context.get()!!.getString(R.string.progress_bar_saving_pattern))
            db.knitPatternDao().saveNewPattern(knitPattern, thumbnail, context.get()!!)
        }

        override fun onProgressUpdate(vararg values: String) {
            progressbarDialog.updateText(values[0])
        }

        override fun onPostExecute(pattern: KnitPattern?) {
            super.onPostExecute(pattern)
            if (pattern != null) {
                context.get()!!.setKnitPattern(pattern, knitPatternDrawer, thumbnail)
            }
            progressbarDialog.dismiss()
        }
    }

    private inner class MySharedPreferenceListener : OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (key == "pattern" && !sharedPreferences.contains("pattern")) {
                clearKnitPattern()
            }
        }
    }
}
