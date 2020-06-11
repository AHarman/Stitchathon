package com.alexharman.stitchathon.importpattern.importimage

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.RequestCode
import com.alexharman.stitchathon.importpattern.BaseImportPatternDialog

class ImportImageDialog:
        BaseImportPatternDialog<ImportImageContract.View, ImportImageContract.Presenter>(),
        ImportImageContract.View {

    override val view = this
    override lateinit var presenter: ImportImageContract.Presenter
    override val fileOpenRequestCode = RequestCode.READ_EXTERNAL_IMAGE
    override val fileType = "image/*"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialog(R.string.import_image_dialog_title, R.layout.import_image_dialog)
    }

    override fun modifyDialogView(view: View) {
        super.modifyDialogView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.colours_numpicker)
        numberPicker.maxValue = 20
        numberPicker.minValue = 2
        numberPicker.value = 2
        numberPicker.wrapSelectorWheel = false
    }

    override fun getFormValues() {
        super.getFormValues()

        val view = requireView()
        presenter.stitchesWide = view.findViewById<EditText>(R.id.stitches_wide_edittext).text.toString().toIntOrNull()
        presenter.stitchesWide = view.findViewById<EditText>(R.id.stitches_high_edittext).text.toString().toIntOrNull()
        presenter.numColours = view.findViewById<NumberPicker>(R.id.colours_numpicker).value
    }

    override fun verifyFields(): Boolean {
        var formError = super.verifyFields()
        val view = requireView()

        val stitchesWideView = view.findViewById<EditText>(R.id.stitches_wide_edittext)
        if (stitchesWideView.text.isNullOrBlank()) {
            stitchesWideView.error = getString(R.string.empty_string_error)
            formError = true
        }

        val stitchesHighView = view.findViewById<EditText>(R.id.stitches_high_edittext)
        if (stitchesHighView.text.isNullOrBlank()) {
            stitchesHighView.error = getString(R.string.empty_string_error)
            formError = true
        }

        return formError
    }
}
