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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberPicker = view.findViewById<NumberPicker>(R.id.colours_numpicker)
        numberPicker.maxValue = 20
        numberPicker.minValue = 2
        numberPicker.value = 2
        numberPicker.wrapSelectorWheel = false
        numberPicker.setOnValueChangedListener{ _, _, newVal -> presenter.numColours = newVal}
        presenter.numColours = numberPicker.value

        view.findViewById<EditText>(R.id.stitches_high_edittext)
                .addTextChangedListener { newText -> presenter.stitchesHigh = newText.toIntOrNull() }
        view.findViewById<EditText>(R.id.stitches_wide_edittext)
                .addTextChangedListener { newText -> presenter.stitchesWide = newText.toIntOrNull() }

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
        val dialog = requireDialog()

        val stitchesWide = presenter.stitchesWide
        if (stitchesWide == null || stitchesWide < 0) {
            dialog.findViewById<EditText>(R.id.stitches_wide_edittext).error = getString(R.string.empty_string_error)
            formError = true
        }

        val stitchesHigh = presenter.stitchesHigh
        if (stitchesHigh == null || stitchesHigh < 0) {
            dialog.findViewById<EditText>(R.id.stitches_high_edittext).error = getString(R.string.empty_string_error)
            formError = true
        }

        return formError
    }
}
