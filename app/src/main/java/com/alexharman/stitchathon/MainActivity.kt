package com.alexharman.stitchathon

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.database.AppDatabase
import com.alexharman.stitchathon.databaseAccessAsyncTasks.*

class MainActivity :
        AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        ImportImageDialog.ImportImageDialogListener, OpenPattern {

    companion object {
        internal lateinit var db: AppDatabase
        const val READ_EXTERNAL_IMAGE = 42
        const val READ_EXTERNAL_JSON_PATTERN = 55
        const val KNIT_PATTERN_FRAGMENT = "KnitPatternFragment"
        const val OPEN_PATTERN_FRAGMENT = "OpenPatternFragment"
        const val SETTINGS_FRAGMENT = "SettingsFragment"
    }

    private var importImageDialog: ImportImageDialog? = null
    private var knitPatternFragment = KnitPatternFragment()
    private var progressbarDialog: ProgressbarDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setUpUI()
        db = AppDatabase.getAppDatabase(applicationContext)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, knitPatternFragment, KNIT_PATTERN_FRAGMENT)
                .commit()

        val patternName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PreferenceKeys.CURRENT_PATTERN_NAME, null)
        if (patternName != null) openPattern(patternName)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            returnToKnitPatternFragment()
        } else {
            super.onBackPressed()
        }
    }

    fun returnToKnitPatternFragment() {
        val fragManager = supportFragmentManager ?: return
        if (fragManager.backStackEntryCount > 0) {
            fragManager.popBackStack(fragManager.getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        when (id) {
            R.id.nav_open -> startFragment(supportFragmentManager.findFragmentByTag(OPEN_PATTERN_FRAGMENT) ?: OpenPatternFragment(), OPEN_PATTERN_FRAGMENT)
            R.id.nav_import_pattern -> selectExternalFile("application/json", READ_EXTERNAL_JSON_PATTERN)
            R.id.nav_import_image -> importImage()
            R.id.nav_about_app -> AppInfoDialog().show(supportFragmentManager, "App info")
            R.id.nav_settings -> startFragment(supportFragmentManager.findFragmentByTag(SETTINGS_FRAGMENT) ?: SettingsFragment(), SETTINGS_FRAGMENT)
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
    }

    private fun startFragment(fragment: Fragment, tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag) != null) {
            supportFragmentManager.popBackStack(tag, 0)
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .addToBackStack(tag)
                    .commit()
        }
    }

    internal fun selectExternalFile(type: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        startActivityForResult(intent, requestCode)
    }

    private fun showProgressBar(title: String, message: String) {
        if (progressbarDialog == null) {
            progressbarDialog = ProgressbarDialog.newInstance(title, message)
            progressbarDialog?.show(supportFragmentManager, "Progress dialog")
        }
    }

    fun openPattern(patternName: String) {
        showProgressBar(getString(R.string.progress_dialog_load_title), getString(R.string.progress_bar_loading_pattern))
        OpenPatternTask(this, this).execute(patternName)
    }

    private fun importImage() {
        importImageDialog = ImportImageDialog()
        importImageDialog?.show(supportFragmentManager, "Importing image")
    }

    private fun importJson(uri: Uri) {
        showProgressBar(getString(R.string.progress_dialog_import_title), getString(R.string.progress_bar_importing_pattern))
        ImportJsonTask(this, this).execute(uri)
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
    }

    fun deletePatterns(vararg patternNames: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        DeletePatternsTask(this).execute(*patternNames)
        if (prefs.getString(PreferenceKeys.CURRENT_PATTERN_NAME, "") in patternNames) {
            knitPatternFragment.clearKnitPattern()
            prefs
                    .edit()
                    .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                    .apply()
        }
    }

    fun deleteAllPatterns() {
        DeleteAllPatternsTask(this).execute()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                .apply()
        knitPatternFragment.clearKnitPattern()
    }

    override fun onImportImageDialogOK(imageUri: Uri, name: String, width: Int, height: Int, oddRowsOpposite: Boolean, numColours: Int) {
        showProgressBar(getString(R.string.progress_dialog_import_title), getString(R.string.progress_bar_importing_pattern))
        ImportImageTask(this, this, imageUri, name, width, height, oddRowsOpposite, numColours).execute()
    }

    override fun onPatternReturned(knitPattern: KnitPattern, knitPatternDrawer: KnitPatternDrawer, thumbnail: Bitmap) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PreferenceKeys.CURRENT_PATTERN_NAME, knitPattern.name)
                .apply()
        progressbarDialog?.dismiss()
        progressbarDialog = null

        returnToKnitPatternFragment()
        knitPatternFragment.onPatternReturned(knitPattern, knitPatternDrawer, thumbnail)
    }
}
