package com.alexharman.stitchathon

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alexharman.stitchathon.database.AppDatabase
import com.jaredrummler.android.colorpicker.ColorPreference
import java.lang.ref.WeakReference

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val RESET_ALL_PREFS: Int = 0
        const val DELETE_ALL_PATTERNS: Int = 1
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null || sharedPreferences == null) return
        if (key == getString(R.string.app_options_stitch_size_key) ||
                key == getString(R.string.app_options_stitch_pad_key)) {
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
        findPreference(getString(R.string.app_options_reset_prefs_key)).onPreferenceClickListener =
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
        findPreference(getString(R.string.app_options_delete_all_key)).onPreferenceClickListener =
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
        var key = getString(R.string.app_options_stitch_size_key)
        findPreference(key).summary = preferenceScreen.sharedPreferences.getString(key, "")
        key = getString(R.string.app_options_stitch_pad_key)
        findPreference(key).summary = preferenceScreen.sharedPreferences.getString(key, "")
        (findPreference(getString(R.string.app_options_stitch_colour_key_1)) as ColorPreference)
                .saveValue(ResourcesCompat.getColor(resources, R.color.default_stitch_colour_1, null))
        (findPreference(getString(R.string.app_options_stitch_colour_key_2)) as ColorPreference)
                .saveValue(ResourcesCompat.getColor(resources, R.color.default_stitch_colour_2, null))
        (findPreference(getString(R.string.app_options_stitch_colour_key_3)) as ColorPreference)
                .saveValue(ResourcesCompat.getColor(resources, R.color.default_stitch_colour_3, null))
    }

    private fun onDialogConfirm(opcode: Int) {
        if (opcode == DELETE_ALL_PATTERNS) {
            val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
            editor.remove("pattern")
            editor.apply()
            DeleteAllAsyncTask(activity).execute()
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

    private class DeleteAllAsyncTask(context: Context) : AsyncTask<Void, Void, Void>() {
        var context: WeakReference<Context> = WeakReference(context)
        override fun doInBackground(vararg params: Void): Void? {
            AppDatabase.getAppDatabase(context.get()!!).knitPatternDao().deleteAllPatterns(context.get()!!)
            return null
        }

    }
}
