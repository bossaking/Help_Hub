package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import android.widget.EditText;



import com.example.help_hub.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddForbiddenWordDialog extends Dialog implements TextWatcher {



    private final Activity myActivity;
    private AlertDialog dialog;
    private EditText mWord;
    private Drawable defaultBackground;

    public AddForbiddenWordDialog(Activity myActivity) {this.myActivity = myActivity;}

    public void startAddForbiddenWordDialog() {
        mWord = new EditText(myActivity.getApplicationContext());
        defaultBackground = mWord.getBackground();
        mWord.setInputType(InputType.TYPE_CLASS_TEXT);
        mWord.addTextChangedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setTitle(R.string.add_forbidden_word_title);
        builder.setMessage(R.string.add_forbidden_word_message);
        builder.setView(mWord);
        builder.setPositiveButton(R.string.add_forbidden_word_button, null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
                FirebaseFirestore.getInstance().collection("forbiddenWords").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) { //sprawdzamy czy słowa nie ma już w bazie
                    if (Objects.equals(document.get("word"), mWord.getText().toString())) {
                        dialog.dismiss();
                    }
                }
                Map<String, String> docData = new HashMap<>();
                docData.put("word", mWord.getText().toString());
                FirebaseFirestore.getInstance().collection("forbiddenWords").add(docData);

                dismissDialog();
                dialog.dismiss();

            }
        }));
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
