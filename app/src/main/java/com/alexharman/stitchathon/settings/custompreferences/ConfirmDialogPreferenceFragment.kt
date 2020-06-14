package com.alexharman.stitchathon.settings.custompreferences

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat


class ConfirmDialogPreferenceFragment(): PreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(preferenceKey: String): ConfirmDialogPreferenceFragment {
            val args = Bundle()
            args.putString(ARG_KEY, preferenceKey)

            val fragment = ConfirmDialogPreferenceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val pref = preference as? ConfirmDialogPreference
            pref?.listener?.onDialogConfirm()
        }
    }
}