package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.help_hub.Activities.AddNewNoticeActivity;
import com.example.help_hub.Activities.AddTheOfferActivity;

public class SelectTypeOfAdvertisement {

    private Activity myActivity;

    private AlertDialog dialog;

    public SelectTypeOfAdvertisement(Activity myActivity) {
        this.myActivity = myActivity;
    }

    public void startSelectTypeOfAdvertisement() {
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setPositiveButton("Offer", null);
        builder.setNegativeButton("Announcement", null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            myActivity.startActivity(new Intent(myActivity.getApplicationContext(), AddTheOfferActivity.class));
            dialog.dismiss();
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            myActivity.startActivity(new Intent(myActivity.getApplicationContext(), AddNewNoticeActivity.class));
            dialog.dismiss();
        });
    }
}
