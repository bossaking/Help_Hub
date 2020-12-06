package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordDialog implements TextWatcher {

    private Activity myActivity;
    private AlertDialog dialog;
    private EditText mPassword;
    private Drawable defaultBackground;

    public ChangePasswordDialog(Activity myActivity) {
        this.myActivity = myActivity;
    }

    public void StartChangePasswordDialog() {

        mPassword = new EditText(myActivity.getApplicationContext());
        defaultBackground = mPassword.getBackground();
        mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        mPassword.addTextChangedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setTitle(R.string.change_password_title);
        builder.setMessage(R.string.change_password_message);
        builder.setView(mPassword);
        builder.setPositiveButton(R.string.change_positive_button, null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String password = mPassword.getText().toString().trim();

            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(x -> {
                        Toast.makeText(myActivity.getApplicationContext(), "Change link sent to your e-mail", Toast.LENGTH_LONG).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(myActivity.getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                } else {
                    Toast.makeText(myActivity.getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                }
            });
            dialog.dismiss();
        });

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mPassword.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
