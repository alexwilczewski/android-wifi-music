package com.WifiAudioDistribution.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.WifiAudioDistribution.R;

public class EditPodDialogFragment extends DialogFragment {
    public interface EditPodDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String podName);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private EditPodDialogListener mListener;
    private String defaultPodName = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View inflater = getActivity().getLayoutInflater().inflate(R.layout.edit_pod, null);
        final EditText mPodNameEditText = (EditText) inflater.findViewById(R.id.edit_pod_name);
        mPodNameEditText.setText(defaultPodName);

        builder.setTitle("Enter Pod Title")
                .setView(inflater)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String mPodName = mPodNameEditText.getText().toString();
                        mListener.onDialogPositiveClick(EditPodDialogFragment.this, mPodName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(EditPodDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EditPodDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void setDefaultPodName(String podName) {
        defaultPodName = podName;
    }
}
