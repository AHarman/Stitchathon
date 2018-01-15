package com.alexharman.stitchathon

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import com.alexharman.stitchathon.MainActivity.READ_EXTERNAL_IMAGE

/**
 * Created by Alex on 08/01/2018.
 */

class ImportImageDialog: DialogFragment() {
    private lateinit var dialogView: View
    private var imageUri: Uri? = null

    interface ImportImageDialogListener {
        fun onImportImageDialogOK(imageUri: Uri, name: String, width: Int, height: Int, numColours: Int)
    }

    // Use this instance of the interface to deliver action events
    private lateinit var listener: ImportImageDialogListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = activity as ImportImageDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement ImportImageDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog
        val inflater = activity.layoutInflater
        val builder = AlertDialog.Builder(activity)
        dialogView = inflater.inflate(R.layout.import_image_dialog, null)

        val numberPicker: NumberPicker = dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker
        numberPicker.maxValue = 20
        numberPicker.minValue = 2
        numberPicker.value = 2
        numberPicker.wrapSelectorWheel = false

        (dialogView.findViewById(R.id.stitches_wide_edittext) as EditText).setOnFocusChangeListener({ v, hf -> emptyTextCheck(v, hf) })
        (dialogView.findViewById(R.id.stitches_high_edittext) as EditText).setOnFocusChangeListener({ v, hf -> emptyTextCheck(v, hf) })
        dialogView.findViewById(R.id.import_image_browse_button).setOnClickListener { (activity as MainActivity).selectExternalFile("image/*", READ_EXTERNAL_IMAGE)}

        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle(R.string.import_image_dialog_title)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.OK, { d, _ -> returnValues(d) })
        builder.setNegativeButton(R.string.cancel, { _, _ -> dismiss() })
        dialog = builder.create()
        dialog.setOnShowListener { d: DialogInterface ->  (d as AlertDialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {  returnValues(d) } }

        return dialog
    }

    private fun emptyTextCheck(v: View, hasFocus: Boolean) {
        if (!hasFocus && (v as EditText).text.trim().isEmpty()) {
            v.error = getString(R.string.empty_string_error)
        } else if (!(v as EditText).text.trim().isEmpty()) {
            v.error = null
        }
    }

    private fun returnValues(dialog: DialogInterface) {
        val nameView = dialogView.findViewById(R.id.pattern_name_edittext) as EditText
        var name = nameView.text.toString()
        val widthView = dialogView.findViewById(R.id.stitches_wide_edittext) as EditText
        val width = widthView.text.toString().toIntOrNull()
        val heightView = dialogView.findViewById(R.id.stitches_high_edittext) as EditText
        val height = widthView.text.toString().toIntOrNull()
        val numPicker = dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker
        val numColours = numPicker.value
        var formNotFull = false

        if (name.isEmpty()) name = nameView.hint.toString()

        if (width == null) {
            widthView.error = getString(R.string.empty_string_error)
            formNotFull = true
        }
        if (height == null) {
            heightView.error = getString(R.string.empty_string_error)
            formNotFull = true
        }
        if (formNotFull) return
        if (imageUri == null) return
        dialog.dismiss()
        listener.onImportImageDialogOK(imageUri!!, name, width!!, height!!, numColours)
    }

    private fun getFileDisplayName(uri: Uri): String {
        var fileName = ""
        if (uri.scheme == "content") {
            val cursor = activity.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return fileName
    }

    fun setUri(uri: Uri) {
        imageUri = uri
        val fileName = getFileDisplayName(uri)
        if (fileName == "") return

        (dialogView.findViewById(R.id.import_image_browse_edittext) as EditText).hint = fileName
        (dialogView.findViewById(R.id.pattern_name_edittext) as EditText).hint = fileName.split(Regex("\\..+$"))[0]
    }
}
