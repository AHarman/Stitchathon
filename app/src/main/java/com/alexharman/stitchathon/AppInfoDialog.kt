package com.alexharman.stitchathon

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * Created by Alex on 30/12/2017.
 */

class AppInfoDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as Context)
        val view = activity?.layoutInflater?.inflate(R.layout.app_info_dialog, null)
        val textView = (view?.findViewById(R.id.app_info_textview) as TextView)
        textView.movementMethod = LinkMovementMethod.getInstance()

        builder.setView(view)
        builder.setTitle(R.string.about_app_title)
        builder.setPositiveButton(R.string.OK, null)
        return builder.create()
    }
}
