package com.alexharman.stitchathon

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alexharman.stitchathon.databaseAccessAsyncTasks.DeleteAllPatternsTask

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white) )
        return view
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
            DeleteAllPatternsTask(activity).execute()
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
            val builder = AlertDialog.Builder(activity)
                    .setTitle(arguments.getString("title"))
                    .setMessage(arguments.getString("message"))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.OK, { _, _ -> (targetFragment as SettingsFragment).onDialogConfirm(targetRequestCode) })
            return builder.create()
        }
    }
}
