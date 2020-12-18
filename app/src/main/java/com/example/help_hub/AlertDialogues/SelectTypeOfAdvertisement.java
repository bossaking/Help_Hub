package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

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
        builder.setPositiveButton("I WANT TO HELP", null);
        builder.setNegativeButton("I NEED HELP", null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        layoutParams.weight = 10;

        positiveButton.setLayoutParams(layoutParams);
        negativeButton.setLayoutParams(layoutParams);

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
