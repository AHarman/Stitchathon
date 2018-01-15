package com.alexharman.stitchathon

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.method.LinkMovementMethod
import android.widget.TextView

/**
 * Created by Alex on 30/12/2017.
 */

class AppInfoDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.app_info_dialog, null)
        val textView = (view.findViewById(R.id.app_info_textview) as TextView)
        textView.movementMethod = LinkMovementMethod.getInstance()

        builder.setView(view)
        builder.setTitle(R.string.about_app_title)
        builder.setPositiveButton(R.string.OK, null)
        return builder.create()
    }
}
