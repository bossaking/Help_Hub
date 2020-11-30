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

import java.util.Objects;

public class EditForbiddenWordDialog extends Dialog implements TextWatcher {


    private final Activity myActivity;
    private AlertDialog dialog;
    private EditText mWord;
    private Drawable defaultBackground;
    private final String word;

    public EditForbiddenWordDialog(Activity myActivity, String word) {
        this.myActivity = myActivity;
        this.word = word;
    }

    public void startEditForbiddenWordDialog() {
        mWord = new EditText(myActivity.getApplicationContext());
        defaultBackground = mWord.getBackground();
        mWord.setInputType(InputType.TYPE_CLASS_TEXT);
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
            words.collection("forbiddenWords").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        if (Objects.requireNonNull(document.get("word")).toString().equals(word)) {
                            words.collection("forbiddenWords").document(document.getId()).update("word", mWord.getText().toString());
                            dismissDialog();
                            dialog.dismiss();
                        }
                    }
                }
            });
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            FirebaseFirestore words = FirebaseFirestore.getInstance();
            words.collection("forbiddenWords").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        if (Objects.requireNonNull(document.get("word")).toString().equals(word)) {
                            words.collection("forbiddenWords").document(document.getId()).delete();
                            dismissDialog();
                            dialog.dismiss();
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
