package com.alexharman.stitchathon.settings.custompreferences

import android.content.Context
import android.content.res.TypedArray
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class EditNumberTextPreference(context: Context, attrs: AttributeSet?): EditTextPreference(context, attrs) {

    init {
        setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return super.getPersistedInt(0).toString()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getInt(index, 0)
    }

    override fun getText(): String {
        return getPersistedString(null)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultInt = defaultValue as? Int ?:  0
        persistInt(getPersistedInt(defaultInt))
        notifyChanged()
    }

    override fun persistString(value: String?): Boolean {
        val number = value?.toIntOrNull()

        return if (number != null)
                persistInt(value.toInt())
            else
                isPersistent
    }
}