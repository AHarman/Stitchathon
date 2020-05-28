package com.alexharman.stitchathon.importimage

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.RadioGroup
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.MainActivity
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.RequestCodes
import com.alexharman.stitchathon.loading.ProgressbarDialog

class ImportImageDialog: BaseDialogFragmentView<ImportImageContract.View, ImportImageContract.Presenter>(),
       ImportImageContract.View {

    override val view = this
    override lateinit var presenter: ImportImageContract.Presenter
    // TODO: Do we need this as a property? Can we just do this.findViewById?
    private lateinit var dialogView: View
    private var progressbarDialog: ProgressbarDialog? = null
    private var imageUri: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog
        val inflater = activity!!.layoutInflater
        val builder = AlertDialog.Builder(activity as Context)
        dialogView = inflater.inflate(R.layout.import_image_dialog, null)

        val numberPicker: NumberPicker = dialogView.findViewById(R.id.import_image_numpicker) as NumberPicker
        numberPicker.maxValue = 20
        numberPicker.minValue = 2
        numberPicker.value = 2
        numberPicker.wrapSelectorWheel = false

        dialogView.findViewById<EditText>(R.id.stitches_wide_edittext).setOnFocusChangeListener({ v, hf -> emptyTextCheck(v as EditText, hf) })
        dialogView.findViewById<EditText>(R.id.stitches_high_edittext).setOnFocusChangeListener({ v, hf -> emptyTextCheck(v as EditText, hf) })
        // TODO: Implement router, which can register the recipients for different codes. Then we can call StartActivtiyForResult directly from here (or via the navigator!).
        dialogView.findViewById<Button>(R.id.import_image_browse_button).setOnClickListener( { (activity as MainActivity).selectExternalFile("image/*", RequestCodes.READ_EXTERNAL_IMAGE.value)})

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

    // TODO: Split out show/dismissLoadingBar into a different interface to share with KnitPatternFragment?
    override fun showLoadingBar() {
        if (progressbarDialog == null) {
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_load_title), getString(R.string.progress_bar_loading_pattern))
            progressbarDialog?.show(fragmentManager, "Progress dialog")
        }
    }

    override fun dismissLoadingBar() {
        progressbarDialog?.dismiss()
        progressbarDialog = null
    }

    override fun patternImported(pattern: KnitPattern) {
        dismiss()
    }

    private fun emptyTextCheck(v: EditText, hasFocus: Boolean) {
        if (!hasFocus && v.text.trim().isEmpty()) {
            v.error = getString(R.string.empty_string_error)
        } else if (!v.text.trim().isEmpty()) {
            v.error = null
        }
    }

    private fun returnValues(dialog: DialogInterface) {
        val nameView = dialogView.findViewById<EditText>(R.id.pattern_name_edittext)
        var name = nameView.text.toString()
        val widthView = dialogView.findViewById<EditText>(R.id.stitches_wide_edittext)
        val width = widthView.text.toString().toIntOrNull()
        val heightView = dialogView.findViewById<EditText>(R.id.stitches_high_edittext)
        val height = heightView.text.toString().toIntOrNull()
        val rowsOrRounds = dialogView.findViewById<RadioGroup>(R.id.rows_rounds_radiogroup)
        val oddRowsOpposite = rowsOrRounds.checkedRadioButtonId == R.id.rows_radio_button
        val numPicker = dialogView.findViewById<NumberPicker>(R.id.import_image_numpicker)
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
        presenter.importImage(imageUri!!, name, width!!, height!!, oddRowsOpposite, numColours)
        dialog.dismiss()
    }

    private fun getFileDisplayName(uri: Uri): String {
        var fileName = ""
        if (uri.scheme == "content") {
            val cursor = activity!!.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return fileName
    }

    fun setFilenameViewText(uri: Uri) {
        imageUri = uri.toString()
        val fileName = getFileDisplayName(uri)
        if (fileName == "") return

        (dialogView.findViewById(R.id.import_image_browse_edittext) as EditText).hint = fileName
        (dialogView.findViewById(R.id.pattern_name_edittext) as EditText).hint = fileName.split(Regex("\\..+$"))[0]
    }
}
