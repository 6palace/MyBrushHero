package com.sealbluetoothtoothpasteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;import java.lang.Override;

/**
 * Created by Henry on 6/2/2015.
 */
public class brushSuccessDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Recorded your brush!")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MAybe do something later
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
