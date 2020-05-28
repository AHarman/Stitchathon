package com.alexharman.stitchathon.pattern

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.alexharman.stitchathon.R

class GoToStitchDialog: DialogFragment() {
    private lateinit var presenter: PatternContract.Presenter
    private lateinit var dialogView: View

    companion object {
        fun newInstance(currentRow: Int, currentCol: Int): GoToStitchDialog {
            val dialog = GoToStitchDialog()
            val args = Bundle()
            args.putInt("currentRow", currentRow)
            args.putInt("currentCol", currentCol)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            presenter = ((parentFragment ?: activity) as PatternContract.View).presenter
        } catch (e: ClassCastException) {
            throw ClassCastException(parentFragment?.toString() ?: activity.toString() + " must implement PatternContract.View")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val builder = AlertDialog.Builder(activity as Context)
        val currentRow = arguments?.getInt("currentRow") ?: 0
        val currentCol = arguments?.getInt("currentCol") ?: 0

        dialogView = activity!!.layoutInflater.inflate(R.layout.go_to_stitch_dialog, null)

        dialogView.findViewById<EditText>(R.id.go_to_stitch_row_edittext)?.hint = currentRow.toString()
        dialogView.findViewById<EditText>(R.id.go_to_stitch_col_edittext)?.hint = currentCol.toString()

        return builder
                .setView(dialogView)
                .setCancelable(false)
                .setTitle(getString(R.string.go_to_dialog_title))
                .setMessage(getString(R.string.go_to_dialog_message))
                .setCancelable(true)
                .setPositiveButton(R.string.OK) { _, _ -> returnValues() }
                .setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
                .create()
    }

    private fun returnValues() {
        val rowEditText = dialogView.findViewById<EditText>(R.id.go_to_stitch_row_edittext)
        val colEditText = dialogView.findViewById<EditText>(R.id.go_to_stitch_col_edittext)
        val row = rowEditText.text.toString().toIntOrNull() ?: rowEditText.hint.toString().toIntOrNull() ?: -1
        val col = colEditText.text.toString().toIntOrNull() ?: colEditText.hint.toString().toIntOrNull() ?: -1

        presenter.goTo(row, col)
        dialog.dismiss()
    }
}