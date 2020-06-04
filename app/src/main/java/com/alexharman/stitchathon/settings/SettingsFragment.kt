package com.alexharman.stitchathon.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.repository.PreferenceKeys
import com.kunzisoft.androidclearchroma.ChromaPreferenceFragmentCompat

class SettingsFragment: ChromaPreferenceFragmentCompat(), SettingsContract.View {
    override lateinit var presenter: SettingsContract.Presenter

    companion object {
        const val RESET_ALL_PREFS: Int = 0
        const val DELETE_ALL_PATTERNS: Int = 1
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setPrefListeners()
    }

    private fun setPrefListeners() {
        findPreference<Preference>(PreferenceKeys.RESET_PREFS)?.onPreferenceClickListener = OnPreferenceClickListener {
            displayConfirmDialog(
                    getString(R.string.reset_application_preferences_title),
                    getString(R.string.reset_application_preferences_message),
                    RESET_ALL_PREFS,
                    "ResetPrefs")
            true
        }
        findPreference<Preference>(PreferenceKeys.DELETE_ALL)?.onPreferenceClickListener = OnPreferenceClickListener {
            displayConfirmDialog(
                    getString(R.string.delete_all_patterns_dialog_title),
                    getString(R.string.delete_all_patterns_dialog_message),
                    DELETE_ALL_PATTERNS,
                    "DeleteAll")
            true
        }
    }

    private fun displayConfirmDialog(title: String, message: String, opcode: Int, tag: String) {
        val bundle = Bundle()
        val dialog = ConfirmDialogFragment()
        bundle.putString("title", title)
        bundle.putString("message", message)
        dialog.arguments = bundle
        dialog.setTargetFragment(this, opcode)
        dialog.show(parentFragmentManager, tag)
    }

    fun onDialogConfirm(opcode: Int) {
        if (opcode == DELETE_ALL_PATTERNS) {
            presenter.deleteAllPatterns()
        } else if (opcode == RESET_ALL_PREFS) {
            presenter.clearAllPreferences()
        }
    }

    inner class ConfirmDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity as Context)
                    .setTitle(arguments?.getString("title"))
                    .setMessage(arguments?.getString("message"))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.OK) { _, _ -> this@SettingsFragment.onDialogConfirm(targetRequestCode) }
            return builder.create()
        }
    }
}
