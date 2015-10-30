package com.sealbluetoothtoothpasteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.lang.Override;

/**
 * Created by Henry on 6/2/2015.
 */
public class BrushSuccessDialog extends DialogFragment {

    public static final String TAG = "BrushSuccessDialog";
    public static final String DISPLAYWEIGHT = "display weight";
    public static final String LIMITWEIGHT = "limit weight";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String usedWeight = getArguments().getString(DISPLAYWEIGHT);
        String limitWeight = getArguments().getString(LIMITWEIGHT);
        Log.d(TAG, "extracted weight baseline: " + limitWeight);

        String message = String.format("Recorded your brush! toothpaste used: " + usedWeight + " grams");
        Float diff = Float.parseFloat(limitWeight) - Float.parseFloat(usedWeight);
        if(diff >= 0.0){
            message = String.format("You didn't use enough toothpaste! you need %.1f more grams.", diff);
        }
        builder.setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MAybe do something later
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
