package com.example.help_hub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditForbiddenWordDialog implements TextWatcher {

    private Activity myActivity;
    private AlertDialog dialog;
    private EditText mWord;
    private Drawable defaultBackground;
    private String word;

    public EditForbiddenWordDialog(Activity myActivity, String word) {
        this.myActivity = myActivity;
        this.word = word;
    }

    public void startEditForbiddenWordDialog() {
        mWord = new EditText(myActivity.getApplicationContext());
        defaultBackground = mWord.getBackground();
        mWord.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        mWord.addTextChangedListener(this);
        mWord.setText(word);

        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setTitle(R.string.edit_forbidden_word_title);
        builder.setMessage(R.string.edit_forbidden_word_message);
        builder.setView(mWord);
        builder.setNegativeButton(R.string.edit_forbidden_word_button_delete, null);
        builder.setPositiveButton(R.string.edit_forbidden_word_button_edit, null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            FirebaseFirestore words = FirebaseFirestore.getInstance();
            words.collection("forbiddenWords").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("word").toString().equals(word)) {
                                words.collection("forbiddenWords").document(document.getId()).update("word", mWord.getText().toString());
                                myActivity.recreate();
                                dialog.dismiss();
                                return;
                            }
                        }
                    }
                }
            });
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            FirebaseFirestore words = FirebaseFirestore.getInstance();
            words.collection("forbiddenWords").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("word").toString().equals(word)) {
                                words.collection("forbiddenWords").document(document.getId()).delete();
                                myActivity.recreate();
                                dialog.dismiss();
                                return;
                            }
                        }
                    }
                }
            });
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
