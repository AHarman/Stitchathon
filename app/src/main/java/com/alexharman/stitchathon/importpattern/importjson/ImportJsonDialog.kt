package com.alexharman.stitchathon.importpattern.importjson

import android.app.Dialog
import android.os.Bundle
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.RequestCode
import com.alexharman.stitchathon.importpattern.BaseImportPatternDialog

class ImportJsonDialog:
        BaseImportPatternDialog<ImportJsonContract.View, ImportJsonContract.Presenter>(),
        ImportJsonContract.View {

    override val view = this
    override lateinit var presenter: ImportJsonContract.Presenter
    override val fileOpenRequestCode = RequestCode.READ_EXTERNAL_JSON_PATTERN
    override val fileType = "application/json"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialog(R.string.import_json_dialog_title, R.layout.import_json_dialog)
    }
}
