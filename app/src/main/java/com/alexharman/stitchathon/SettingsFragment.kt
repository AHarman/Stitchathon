package com.alexharman.stitchathon

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SettingsFragment: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

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
        var key = getString(R.string.app_options_stitch_size_key)
        findPreference(key).summary = preferenceScreen.sharedPreferences.getString(key, "")
        key = getString(R.string.app_options_stitch_pad_key)
        findPreference(key).summary = preferenceScreen.sharedPreferences.getString(key, "")
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
}
