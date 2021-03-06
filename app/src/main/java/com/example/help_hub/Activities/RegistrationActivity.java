package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class RegistrationActivity extends AppCompatActivity implements TextWatcher {

    private EditText mEmail, mPassword, mRepeatPassword;
    private Button signUpButton;
    private Drawable defaultEditTextDrawable;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        TextView goToLoginActivity = findViewById(R.id.go_to_login);
        goToLoginActivity.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        firebaseAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.registration_email);
        defaultEditTextDrawable = mEmail.getBackground();
        mPassword = findViewById(R.id.registration_password_text);
        mRepeatPassword = findViewById(R.id.registration_repeat_password_text);

        mEmail.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mRepeatPassword.addTextChangedListener(this);

        signUpButton = findViewById(R.id.registration_button);

        //Gdy naciskamy przycisk rejestracji
        signUpButton.setOnClickListener(v -> {
            final String email = mEmail.getText().toString().trim();
            final String password = mPassword.getText().toString().trim();

            String repeatPassword = mRepeatPassword.getText().toString().trim();

            if (!validateEmail(email) || !validatePassword(password, repeatPassword)) return;

            final LoadingDialog loadingDialog = new LoadingDialog(RegistrationActivity.this);
            loadingDialog.StartLoadingDialog();

            //Sprawdzamy, czy podany E-mail nie jest już zarejestrowany
            firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> methods = task.getResult().getSignInMethods();

                            if (!methods.isEmpty()) {
                                mEmail.setError(getString(R.string.email_exists_error));
                                mEmail.setBackgroundResource(R.drawable.edit_error_border);
                                mEmail.requestFocus();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), NewUserBasicInformationActivity.class);
                                intent.putExtra("USER_EMAIL", email);
                                intent.putExtra("USER_PASSWORD", password);
                                startActivity(intent);
                                finish();
                            }
                        }

                        loadingDialog.DismissDialog();
                    });
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            mEmail.setError(getString(R.string.email_empty_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError(getString(R.string.email_validate_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password, String repeatPassword) {
        if (password.length() < 8) {
            mPassword.setError(getString(R.string.password_length_error));
            mPassword.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if (!password.equals(repeatPassword)) {
            mRepeatPassword.setError(getString(R.string.password_match_error));
            mRepeatPassword.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mRepeatPassword.setBackground(defaultEditTextDrawable);
        mPassword.setBackground(defaultEditTextDrawable);
        mEmail.setBackground(defaultEditTextDrawable);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
