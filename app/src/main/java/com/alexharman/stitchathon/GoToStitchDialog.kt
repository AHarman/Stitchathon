package com.alexharman.stitchathon

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText

class GoToStitchDialog: DialogFragment() {
    interface GoToStitchDialogListener {
        fun onGoToStitchReturn(row: Int, col: Int)
    }

    private lateinit var listener: GoToStitchDialogListener
    private lateinit var dialogView: View

    companion object {
        fun newInstance(row: Int, col: Int): GoToStitchDialog {
            val dialog = GoToStitchDialog()
            val args = Bundle()
            args.putInt("row", row)
            args.putInt("col", col)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = (parentFragment ?: activity) as GoToStitchDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement GoToStitchDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(activity as Context)
        dialogView = activity!!.layoutInflater.inflate(R.layout.go_to_stitch_dialog, null)

        dialogView.findViewById<EditText>(R.id.go_to_stitch_row_edittext).hint = (arguments?.getInt("row") ?: 0).toString()
        dialogView.findViewById<EditText>(R.id.go_to_stitch_col_edittext).hint = (arguments?.getInt("col") ?: 0).toString()

        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.go_to_dialog_title))
        // Todo: Remove second sentence, pass in row and col and set them as the hint text/default
        builder.setMessage(getString(R.string.go_to_dialog_message))
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.OK, { _, _ -> returnValues() } )
        builder.setNegativeButton(R.string.cancel, { _, _ -> dismiss() } )
        dialog = builder.create()
        return dialog
    }

    private fun returnValues() {
        val rowEditText = dialogView.findViewById<EditText>(R.id.go_to_stitch_row_edittext)
        val colEditText = dialogView.findViewById<EditText>(R.id.go_to_stitch_col_edittext)
        val row = rowEditText.text.toString().toIntOrNull() ?: rowEditText.hint.toString().toIntOrNull() ?: -1
        val col = colEditText.text.toString().toIntOrNull() ?: colEditText.hint.toString().toIntOrNull() ?: -1

        listener.onGoToStitchReturn(row, col)
        dialog.dismiss()
    }
}