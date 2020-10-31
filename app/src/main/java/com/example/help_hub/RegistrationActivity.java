package com.example.help_hub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class RegistrationActivity extends AppCompatActivity implements TextWatcher {

    EditText mEmail, mPassword, mRepeatPassword;
    Button signUpButton;

    Drawable defaultEditTextDrawable;

    FirebaseAuth firebaseAuth;

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

        mEmail.addTextChangedListener(this);

        mPassword = findViewById(R.id.registration_password);
        mPassword.addTextChangedListener(this);

        mRepeatPassword = findViewById(R.id.registration_repeat_password);
        mRepeatPassword.addTextChangedListener(this);

        signUpButton = findViewById(R.id.registration_button);

        //Gdy naciskamy przycisk rejestracji
        signUpButton.setOnClickListener(v -> {

            final String email = mEmail.getText().toString().trim();
            final String password = mPassword.getText().toString().trim();
            String repeatPassword = mRepeatPassword.getText().toString().trim();

            if(!ValidateEmail(email) || !ValidatePassword(password, repeatPassword)) {
                return;
            }

            final LoadingDialog loadingDialog = new LoadingDialog(RegistrationActivity.this);
            loadingDialog.StartLoadingDialog();

            //Sprawdzamy, czy podany E-mail nie jest już zarejestrowany
            firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if(task.isSuccessful()){
                        List<String> methods = task.getResult().getSignInMethods();
                        if(!methods.isEmpty()){

                            mEmail.setError(getString(R.string.email_exists_error));
                            mEmail.setBackgroundResource(R.drawable.edit_error_border);
                            mEmail.requestFocus();
                        }else{
                            Intent intent = new Intent(getApplicationContext(), NewUserBasicInformationsActivity.class);
                            intent.putExtra("USER_EMAIL", email);
                            intent.putExtra("USER_PASSWORD", password);
                            startActivity(intent);
                            finish();
                        }
                    }
                    loadingDialog.DismissDialog();
                }
            });
        });

    }

    private boolean ValidateEmail(String email){

        if(email.isEmpty()){
            mEmail.setError(getString(R.string.email_empty_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError(getString(R.string.email_validate_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return true;
    }

    private boolean ValidatePassword(String password, String repeatPassword){

        if(password.length() < 8){
            mPassword.setError(getString(R.string.password_length_error));
            mPassword.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if(!password.equals(repeatPassword)){
            mRepeatPassword.setError(getString(R.string.password_match_error));
            mRepeatPassword.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return  true;
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
