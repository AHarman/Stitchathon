package com.alexharman.stitchathon

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference.OnPreferenceClickListener
import android.support.v7.preference.PreferenceManager
import com.kunzisoft.androidclearchroma.ChromaPreferenceFragmentCompat

class SettingsFragment: ChromaPreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val RESET_ALL_PREFS: Int = 0
        const val DELETE_ALL_PATTERNS: Int = 1
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null || sharedPreferences == null) return
        if (key == PreferenceKeys.STITCH_SIZE ||
                key == PreferenceKeys.STITCH_PAD) {
            findPreference(key).summary = sharedPreferences.getString(key, "")
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        updateSummaryValues()
        setPrefListeners()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun setPrefListeners() {
        findPreference(PreferenceKeys.RESET_PREFS).onPreferenceClickListener =
                OnPreferenceClickListener {
                    val bundle = Bundle()
                    val dialog = ConfirmDialogFragment()
                    bundle.putString("title", getString(R.string.reset_application_preferences_title))
                    bundle.putString("message", getString(R.string.reset_application_preferences_message))
                    dialog.arguments = bundle
                    dialog.setTargetFragment(this, RESET_ALL_PREFS)
                    dialog.show(fragmentManager, "ResetPrefs")
                    true
                }
        findPreference(PreferenceKeys.DELETE_ALL).onPreferenceClickListener =
                OnPreferenceClickListener {
                    val bundle = Bundle()
                    val dialog = ConfirmDialogFragment()
                    bundle.putString("title", getString(R.string.delete_all_patterns_dialog_title))
                    bundle.putString("message", getString(R.string.delete_all_patterns_dialog_message))
                    dialog.arguments = bundle
                    dialog.setTargetFragment(this, DELETE_ALL_PATTERNS)
                    dialog.show(fragmentManager, "DeleteAll")
                    true
                }
    }

    private fun updateSummaryValues() {
        val prefs = preferenceScreen.sharedPreferences
        findPreference(PreferenceKeys.STITCH_SIZE).summary = prefs.getString(PreferenceKeys.STITCH_SIZE, "")
        findPreference(PreferenceKeys.STITCH_PAD).summary = prefs.getString(PreferenceKeys.STITCH_PAD, "")
    }

    fun onDialogConfirm(opcode: Int) {
        if (opcode == DELETE_ALL_PATTERNS) {
            val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
            editor.remove(PreferenceKeys.CURRENT_PATTERN_NAME)
            editor.apply()
            (activity as MainActivity?)?.deleteAllPatterns()
        } else if (opcode == RESET_ALL_PREFS) {
            PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .clear()
                    .apply()
            PreferenceManager.setDefaultValues(activity, R.xml.preferences, true)
            updateSummaryValues()
        }
    }

    class ConfirmDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity as Context)
                    .setTitle(arguments?.getString("title"))
                    .setMessage(arguments?.getString("message"))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.OK, { _, _ -> (targetFragment as SettingsFragment).onDialogConfirm(targetRequestCode) })
            return builder.create()
        }
    }
}
