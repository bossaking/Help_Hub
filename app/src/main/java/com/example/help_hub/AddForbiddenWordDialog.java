package com.example.help_hub;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.WriteResult;

import java.util.HashMap;
import java.util.Map;

public class AddForbiddenWordDialog implements TextWatcher {

    private Activity myActivity;
    private AlertDialog dialog;
    private EditText mWord;
    private Drawable defaultBackground;

    public AddForbiddenWordDialog(Activity myActivity) {this.myActivity = myActivity;}

    public void startAddForbiddenWordDialog() {
        mWord = new EditText(myActivity.getApplicationContext());
        defaultBackground = mWord.getBackground();
        mWord.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        mWord.addTextChangedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setTitle(R.string.add_forbidden_word_title);
        builder.setMessage(R.string.add_forbidden_word_message);
        builder.setView(mWord);
        builder.setPositiveButton(R.string.add_forbidden_word_button, null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, String> docData = new HashMap<>();
            docData.put("word", mWord.getText().toString());
            FirebaseFirestore.getInstance().collection("forbiddenWords").add(docData);
            dialog.dismiss();
        });

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mWord.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
