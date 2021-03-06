package com.alexharman.stitchathon

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.alexharman.stitchathon.importpattern.importimage.ImportImageDialog
import com.alexharman.stitchathon.importpattern.importimage.ImportImagePresenter
import com.alexharman.stitchathon.importpattern.importjson.ImportJsonDialog
import com.alexharman.stitchathon.importpattern.importjson.ImportJsonPresenter
import com.alexharman.stitchathon.pattern.KnitPatternFragment
import com.alexharman.stitchathon.pattern.PatternPresenter
import com.alexharman.stitchathon.repository.KnitPatternRepository
import com.alexharman.stitchathon.repository.PreferenceKeys
import com.alexharman.stitchathon.selectpattern.SelectPatternFragment
import com.alexharman.stitchathon.selectpattern.SelectPatternPresenter
import com.alexharman.stitchathon.settings.SettingsFragment
import com.alexharman.stitchathon.settings.SettingsPresenter
import com.google.android.material.navigation.NavigationView

class MainActivity :
        AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    companion object {
        lateinit var repository: KnitPatternRepository
        const val KNIT_PATTERN_FRAGMENT = "KnitPatternFragment"
        const val OPEN_PATTERN_FRAGMENT = "OpenPatternFragment"
        const val SETTINGS_FRAGMENT = "SettingsFragment"
    }

    private var knitPatternFragment = KnitPatternFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setUpUI()
        repository = KnitPatternRepository.getInstance(this)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, knitPatternFragment, KNIT_PATTERN_FRAGMENT)
                .commit()
        PatternPresenter(knitPatternFragment, repository)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> { drawer.closeDrawer(GravityCompat.START)}
            supportFragmentManager.backStackEntryCount > 0 -> { returnToKnitPatternFragment() }
            else -> { super.onBackPressed() }
        }
    }

    private fun returnToKnitPatternFragment() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack(supportFragmentManager.getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_open -> startSelectPatternFragment()
            R.id.nav_import_pattern -> importJson()
            R.id.nav_import_image -> importImage()
            R.id.nav_about_app -> AppInfoDialog().show(supportFragmentManager, "App info")
            R.id.nav_settings -> showSettings()
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

    // TODO: Untangle this. Start fresh with always creating a new fragment rather than popping & always pop on navigation
    private fun popToOrStartFragment(fragment: Fragment, tag: String) {
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

    private fun showSettings() {
        val fragment =
                supportFragmentManager.findFragmentByTag(SETTINGS_FRAGMENT) as? SettingsFragment ?:
                SettingsFragment()
        SettingsPresenter(fragment, repository)
        popToOrStartFragment(fragment, SETTINGS_FRAGMENT)
    }

    private fun startSelectPatternFragment() {
        val fragment =
                supportFragmentManager.findFragmentByTag(OPEN_PATTERN_FRAGMENT) as? SelectPatternFragment ?:
                SelectPatternFragment()
        SelectPatternPresenter(fragment, repository)
        popToOrStartFragment(fragment, OPEN_PATTERN_FRAGMENT)
    }

    private fun importImage() {
        val dialog = ImportImageDialog()
        ImportImagePresenter(dialog, repository)
        dialog.show(supportFragmentManager, "Importing image")
    }

    private fun importJson() {
        val dialog = ImportJsonDialog()
        ImportJsonPresenter(dialog, repository)
        dialog.show(supportFragmentManager, "Importing image")
    }

    fun deleteAllPatterns() {
        repository.deleteAllKnitPatterns()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .remove(PreferenceKeys.CURRENT_PATTERN_NAME)
                .apply()
        knitPatternFragment.setPattern(null, null)
    }
}
