package com.alexharman.stitchathon

import android.content.Context
import android.support.v7.preference.PreferenceManager
import com.alexharman.stitchathon.repository.PreferenceKeys
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment

@RunWith(RobolectricTestRunner::class)
class SettingsFragmentUnitTests {

    @Test
    fun whenStitchPadChanged_updatePrefSummary() {
        val frag = SettingsFragment()
        startFragment(frag)

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.applicationContext)
                .edit().putString(PreferenceKeys.STITCH_PAD, "55").apply()

        val summary = frag.preferenceScreen.findPreference(PreferenceKeys.STITCH_PAD).summary
        assertEquals(summary, "55")
    }

    @Test
    fun whenStitchSizeChanged_updatePrefSummary() {
        val frag = SettingsFragment()
        startFragment(frag)

        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.applicationContext)
                .edit().putString(PreferenceKeys.STITCH_SIZE, "1A").apply()

        val summary = frag.preferenceScreen.findPreference(PreferenceKeys.STITCH_SIZE).summary
        assertEquals(summary, "1A")
    }

    @Test
    fun ifNonDefaultPrefs_afterOnCreate_stitchSizeSummaryCorrect() {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.applicationContext)
                .edit().putString(PreferenceKeys.STITCH_SIZE, "foo").apply()
        val frag = SettingsFragment()
        startFragment(frag)

        assertEquals(frag.preferenceScreen.findPreference(PreferenceKeys.STITCH_SIZE).summary, "foo")
    }

    @Test
    fun ifNonDefaultPrefs_afterOnCreate_stitchPadSummaryCorrect() {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.applicationContext)
                .edit().putString(PreferenceKeys.STITCH_PAD, "foo").apply()
        val frag = SettingsFragment()
        startFragment(frag)

        assertEquals(frag.preferenceScreen.findPreference(PreferenceKeys.STITCH_PAD).summary, "foo")
    }

    @Test
    fun ifClearPrefsSelected_thenPrefsCleared() {
        val frag = SettingsFragment()
        startFragment(frag)
        val prefs1 = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application.applicationContext)
        val prefs2 = frag.activity?.getSharedPreferences("test", Context.MODE_PRIVATE)
        PreferenceManager.setDefaultValues(frag.activity, "test", Context.MODE_PRIVATE, R.xml.preferences, true)

        frag.onDialogConfirm(SettingsFragment.RESET_ALL_PREFS)

        assert(prefs1.all == prefs2?.all)
    }

    // TODO: Actually have this test run
    // Can't figure out how to work around issues with AsyncTask and Room...
//    @Test
//    fun ifDeleteAllPatterns_clearCurrentPattern() {
//        val frag = Robolectric.buildFragment(SettingsFragment::class.java).create().start().resume().visible().get()
//        val prefs = PreferenceManager.getDefaultSharedPreferences(frag.activity)
//
//        frag.onDialogConfirm(SettingsFragment.DELETE_ALL_PATTERNS)
//
//        assert(!prefs.contains(PreferenceKeys.CURRENT_PATTERN_NAME))
//    }
}