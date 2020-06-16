package com.alexharman.stitchathon.importpattern

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
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
        onViewCreated(dialogView, null)

        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle(titleId)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.OK) { _, _ -> onOkPressed() }
        builder.setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
        dialog = builder.create()

        return dialog
    }

    override fun onResume() {
        super.onResume()
        updateViewData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == fileOpenRequestCode.value && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                presenter.uri = uri.toString()
                if (presenter.name.isNullOrBlank()) {
                    presenter.name = removeFileExtension(getFileName(uri))
                }
            }
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.import_file_browse_button).setOnClickListener { selectFile() }
        view.findViewById<EditText>(R.id.file_uri_edittext).setOnClickListener { selectFile() }
        view.findViewById<EditText>(R.id.pattern_name).addTextChangedListener { newText -> presenter.name = newText }

        val radioGroup = view.findViewById<RadioGroup>(R.id.rows_or_rounds)
        radioGroup.setOnCheckedChangeListener { _, checkedId -> radioButtonPressed(checkedId) }
        radioButtonPressed(radioGroup.checkedRadioButtonId)
    }

    // TODO: I imagine this is where ViewModel comes in handy...
    // Needed because URL field (and therefore name field) may change when view is null.
    private fun updateViewData() {
        val dialog = requireDialog()

        if (!presenter.uri.isNullOrBlank()) {
            val uriView = dialog.findViewById<EditText>(R.id.file_uri_edittext)
            val filename = getFileName(Uri.parse(presenter.uri))
            uriView.setText(filename)
        }

        if (!presenter.name.isNullOrBlank()) {
            dialog.findViewById<EditText>(R.id.pattern_name).setText(presenter.name)
        }
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

    private fun radioButtonPressed(@IdRes checkedId: Int) {
        presenter.oddRowsOpposite = checkedId == R.id.rows_button
    }

    private fun onOkPressed() {
        val fieldErrors = verifyFields()
        if (fieldErrors)
            return

        presenter.importButtonPressed()
    }

    @CallSuper
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

    @CallSuper
    protected open fun verifyFields(): Boolean {
        val dialog = requireDialog()

        if (presenter.uri.isNullOrBlank()) {
            dialog.findViewById<EditText>(R.id.file_uri_edittext).error = getString(R.string.empty_string_error)
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

    protected fun EditText.addTextChangedListener(onTextChanged: (newText: String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged.invoke(s.toString())
            }
        })
    }
}
