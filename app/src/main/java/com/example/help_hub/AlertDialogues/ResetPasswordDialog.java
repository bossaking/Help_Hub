package com.example.help_hub.AlertDialogues;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.help_hub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordDialog implements TextWatcher {

    Activity myActivity;
    EditText mEmail;
    Drawable defaultBackground;
    AlertDialog dialog;

    public ResetPasswordDialog(Activity myActivity){
        this.myActivity = myActivity;
    }

    public void StartResetPasswordDialog(){

        mEmail = new EditText(myActivity.getApplicationContext());
        defaultBackground = mEmail.getBackground();
        mEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mEmail.addTextChangedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setView(mEmail);
        builder.setTitle(R.string.reset_password_title);
        builder.setMessage(R.string.reset_password_message);

        builder.setPositiveButton(R.string.reset_positive_button, null);

        dialog = builder.create();
        builder.setCancelable(true);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();

            if(!ValidateEmail(email)){
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(aVoid ->
                    Toast.makeText(myActivity.getApplicationContext(), "Reset link sent to your e-mail", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e
                    -> Toast.makeText(myActivity.getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

            dialog.dismiss();
        });
    }

    private boolean ValidateEmail(String email){


        if(email.isEmpty()){
            mEmail.setError(myActivity.getString(R.string.email_empty_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError(myActivity.getString(R.string.email_validate_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mEmail.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
