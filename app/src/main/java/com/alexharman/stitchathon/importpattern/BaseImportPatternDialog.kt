package com.alexharman.stitchathon.importpattern

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.alexharman.stitchathon.BaseDialogFragmentView
import com.alexharman.stitchathon.KnitPackage.KnitPattern
import com.alexharman.stitchathon.R
import com.alexharman.stitchathon.RequestCode
import com.alexharman.stitchathon.loading.ProgressbarDialog

abstract class BaseImportPatternDialog<V: BaseImportPatternContract.View<P>, P: BaseImportPatternContract.Presenter<V>>:
        BaseDialogFragmentView<V, P>(),
        BaseImportPatternContract.View<P> {

    private var progressbarDialog: ProgressbarDialog? = null
    private var fileUri: Uri? = null
    protected abstract val fileOpenRequestCode: RequestCode
    protected abstract val fileType: String

    abstract override fun onCreateDialog(savedInstanceState: Bundle?): Dialog

    protected fun createDialog(@StringRes titleId: Int, @LayoutRes layoutId: Int): Dialog {
        val dialog: AlertDialog
        val inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(activity as Context)
        val dialogView = inflater.inflate(layoutId, null)
        modifyDialogView(dialogView)

        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle(R.string.import_image_dialog_title)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.OK) { _, _ -> onOkPressed() }
        builder.setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
        dialog = builder.create()

        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == fileOpenRequestCode.value && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                setUri(uri)
            }
        }
    }

    protected open fun modifyDialogView(view: View) {
        view.findViewById<Button>(R.id.import_file_browse_button).setOnClickListener { selectFile() }
        view.findViewById<EditText>(R.id.file_url_edittext).setOnClickListener { selectFile() }
    }


    override fun showLoadingBar() {
        if (progressbarDialog == null) {
            progressbarDialog = ProgressbarDialog.newInstance(getString(R.string.progress_dialog_load_title), getString(R.string.progress_bar_loading_pattern))
            progressbarDialog?.show(parentFragmentManager, "Progress dialog")
        }
    }

    override fun dismissLoadingBar() {
        progressbarDialog?.dismiss()
        progressbarDialog = null
    }

    override fun patternImported(pattern: KnitPattern) {
        dismiss()
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = fileType
        startActivityForResult(intent, fileOpenRequestCode.value)
    }

    private fun onOkPressed() {
        val fieldErrors = verifyFields()
        if (fieldErrors)
            return

        getFormValues()
        presenter.importButtonPressed()
    }

    protected open fun getFormValues() {
        val view = requireView()
        presenter.uri = fileUri.toString()

        val nameView = view.findViewById<EditText>(R.id.pattern_name)
        var name = nameView.text.toString()
        if (name.isBlank()) {
            name = nameView.hint.toString()
        }
        presenter.name = name

        val rowsRoundsRadio = view.findViewById<RadioGroup>(R.id.rows_or_rounds)
        presenter.oddRowsOpposite = rowsRoundsRadio.checkedRadioButtonId == R.id.rows_button
    }

    protected open fun verifyFields(): Boolean {
        val view = requireView()

        val urlEditText = view.findViewById<EditText>(R.id.file_url_edittext)
        if (urlEditText.text.isNullOrBlank()) {
            urlEditText.error = getString(R.string.empty_string_error)
            return true
        }

        return false
    }

    private fun getFileName(uri: Uri): String {
        var fileName = ""
        if (uri.scheme == "content") {
            val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return fileName
    }

    private fun removeFileExtension(filename: String) = filename.split(Regex("\\..+$"))[0]

    private fun setUri(uri: Uri) {
        fileUri = uri
        val view = getView() ?: return
        val filename = getFileName(uri)
        val filenameNoExt = removeFileExtension(filename)
        (view.findViewById(R.id.file_url_edittext) as EditText).setText(filename)
        (view.findViewById(R.id.pattern_name) as EditText).hint = filenameNoExt
    }
}
