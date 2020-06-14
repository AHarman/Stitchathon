package com.alexharman.stitchathon.settings

import android.os.Bundle
import androidx.preference.Preference
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.repository.PreferenceKeys
import com.alexharman.stitchathon.settings.custompreferences.ConfirmDialogPreference
import com.alexharman.stitchathon.settings.custompreferences.ConfirmDialogPreference.OnDialogConfirmListener
import com.alexharman.stitchathon.settings.custompreferences.ConfirmDialogPreferenceFragment
import com.kunzisoft.androidclearchroma.ChromaPreferenceFragmentCompat

class SettingsFragment: ChromaPreferenceFragmentCompat(), SettingsContract.View {
    override lateinit var presenter: SettingsContract.Presenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setPrefListeners()
    }

    private fun setPrefListeners() {
        findPreference<ConfirmDialogPreference>(PreferenceKeys.RESET_PREFS)
                ?.setOnDialogConfirmListener(object: OnDialogConfirmListener {
                    override fun onDialogConfirm() {
                        presenter.clearAllPreferences()
                    }
                })
        findPreference<ConfirmDialogPreference>(PreferenceKeys.DELETE_ALL)
                ?.setOnDialogConfirmListener(object: OnDialogConfirmListener {
                    override fun onDialogConfirm() {
                        presenter.deleteAllPatterns()
                    }
                })
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is ConfirmDialogPreference) {
            val fragment = ConfirmDialogPreferenceFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "ConfirmDialogPreference")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
