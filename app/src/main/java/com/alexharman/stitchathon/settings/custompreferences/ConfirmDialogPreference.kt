package com.alexharman.stitchathon.settings.custompreferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference


class ConfirmDialogPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {

    interface OnDialogConfirmListener {
        fun onDialogConfirm()
    }

    var listener: OnDialogConfirmListener? = null
        private set

    fun setOnDialogConfirmListener(listener: OnDialogConfirmListener) {
        this.listener = listener
    }
}