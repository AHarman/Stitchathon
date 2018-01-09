package com.alexharman.stitchathon

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker

/**
 * Created by Alex on 08/01/2018.
 */

class ImportImageDialog: DialogFragment() {
    lateinit var dialogView: View

    interface ImportImageDialogListener {
        fun onImportImageDialogOK(name: String, width: Int, height: Int, numColours: Int)
    }

    // Use this instance of the interface to deliver action events
    private lateinit var listener: ImportImageDialogListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = activity as ImportImageDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement NoticeDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog
        val inflater = activity.layoutInflater
        val builder = AlertDialog.Builder(activity)
        dialogView = inflater.inflate(R.layout.import_image_dialog, null)

        val numberPicker: NumberPicker = dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker
        numberPicker.maxValue = 20
        numberPicker.minValue = 2

        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle(R.string.import_image_dialog_title)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.OK, {_, _ -> returnValues()})
        builder.setNegativeButton(R.string.cancel, {_, _ ->  dismiss()})
        dialog = builder.create()

        (dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker).value = 2
        return dialog
    }

    private fun returnValues() {
        val name = (dialogView.findViewById(R.id.pattern_name_edittext) as EditText).text.toString()
        val width = (dialogView.findViewById(R.id.stitches_wide_edittext) as EditText).text.toString().toInt()
        val height = (dialogView.findViewById(R.id.stitches_high_edittext) as EditText).text.toString().toInt()
        val numColours = (dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker).value

        listener.onImportImageDialogOK(name, width, height, numColours)
    }
}