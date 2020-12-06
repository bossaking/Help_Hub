package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.help_hub.R;

public class LoadingDialog {

    Activity myActivity;
    AlertDialog dialog;

    public LoadingDialog(Activity myActivity) {
        this.myActivity = myActivity;
    }

    public void StartLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        LayoutInflater inflater = myActivity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void DismissDialog() {
        dialog.dismiss();
    }

}
