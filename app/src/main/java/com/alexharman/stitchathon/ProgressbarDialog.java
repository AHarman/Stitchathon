package com.alexharman.stitchathon;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Alex on 30/12/2017.
 */

public class ProgressbarDialog extends DialogFragment {
    ProgressBar progressBar;
    View dialogView;

    public static ProgressbarDialog newInstance(String title, boolean indeterminate, String label) {
        Log.d("Dialog", "In newInstance");
        ProgressbarDialog fragment = new ProgressbarDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("label", label);
        args.putBoolean("indeterminate", indeterminate);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("Dialog", "In onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.progress_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setTitle(getArguments().getString("title"));
        Dialog dialog = builder.create();

        progressBar = dialogView.findViewById(R.id.progress_dialog_bar);
        progressBar.setIndeterminate(getArguments().getBoolean("indeterminate"));
        ((TextView) dialogView.findViewById(R.id.progress_dialog_text)).setText(getArguments().getString("label"));

        return dialog;
    }

    public void updateText(String text) {
        ((TextView) dialogView.findViewById(R.id.progress_dialog_text)).setText(text);
    }
}
