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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText mEmail, mPassword, mRepeatPassword;
    Button signUpButton;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mEmail = findViewById(R.id.registration_email);
        final Drawable defaultEditTextDrawable = mEmail.getBackground();

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEmail.setBackground(defaultEditTextDrawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPassword = findViewById(R.id.registration_password);
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPassword.setBackground(defaultEditTextDrawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mRepeatPassword = findViewById(R.id.registration_repeat_password);
        mRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRepeatPassword.setBackground(defaultEditTextDrawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signUpButton = findViewById(R.id.registration_button);

        //Gdy naciskamy przycisk rejestracji
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String repeatPassword = mRepeatPassword.getText().toString().trim();

                if(!ValidateEmail(email) || !ValidatePassword(password, repeatPassword)) {
                    return;
                }

                final LoadingDialog loadingDialog = new LoadingDialog(RegistrationActivity.this);
                loadingDialog.StartLoadingDialog();
                //Próbujemy zarejestrować użytkownika
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    //Jeżeli udało się
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "User created", Toast.LENGTH_SHORT).show();
                            loadingDialog.DismissDialog();

                            //Zamienić przekirowanie z "Main Activity" na "User Activity"
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(RegistrationActivity.this, "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {

                    //Jeżeli się nie udało
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistrationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private boolean ValidateEmail(String email){

        if(email.isEmpty()){
            mEmail.setError(getString(R.string.email_empty_error));
            mEmail.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
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
}
