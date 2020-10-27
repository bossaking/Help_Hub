package com.example.help_hub;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {

    Activity myActivity;
    AlertDialog dialog;

    public LoadingDialog(Activity myActivity){
        this.myActivity = myActivity;
    }

    void StartLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        LayoutInflater inflater = myActivity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    void DismissDialog(){
        dialog.dismiss();
    }

}
