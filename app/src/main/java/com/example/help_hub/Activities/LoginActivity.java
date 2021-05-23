package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.AlertDialogues.ResetPasswordDialog;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements TextWatcher {

    EditText mEmail, mPassword;
    Button mLoginButton;
    TextView goToRegistration, loginErrorSpan, forgotPassword;
    Drawable defaultTextView;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.StartLoadingDialog();
            getTokenAndStartActivity();
        }

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password_text);
        mLoginButton = findViewById(R.id.login_button);
        goToRegistration = findViewById(R.id.go_to_registration);
        forgotPassword = findViewById(R.id.forgot_password);
        loginErrorSpan = findViewById(R.id.login_error_span);

        defaultTextView = mEmail.getBackground();

        goToRegistration.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            finish();
        });

        mEmail.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);

        mLoginButton.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            final LoadingDialog loadingDialog = new LoadingDialog(LoginActivity.this);
            loadingDialog.StartLoadingDialog();

            if (!ValidateEmail(email) || !ValidatePassword(password)) {
                loadingDialog.DismissDialog();
                return;
            }


            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();

                    getTokenAndStartActivity();

                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + task.getException(), Toast.LENGTH_LONG).show();
                    loadingDialog.DismissDialog();
                }
            }).addOnFailureListener(e -> {
                loginErrorSpan.setVisibility(View.VISIBLE);
                loadingDialog.DismissDialog();
            });
        });

        forgotPassword.setOnClickListener(v -> {
            ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog(LoginActivity.this);
            resetPasswordDialog.StartResetPasswordDialog();
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        loginErrorSpan.setVisibility(View.GONE);
        mEmail.setBackground(defaultTextView);
        mPassword.setBackground(defaultTextView);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean ValidateEmail(String email) {

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

    private boolean ValidatePassword(String password) {

        if (password.length() < 8) {
            mPassword.setError(getString(R.string.password_length_error));
            mPassword.setBackgroundResource(R.drawable.edit_error_border);
            return false;
        }

        return true;
    }

    private void getTokenAndStartActivity(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, instanceIdResult -> {
            String token = instanceIdResult.getToken();
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid()).removeValue().
                    addOnSuccessListener(unused -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").
                                child(FirebaseAuth.getInstance().getUid());
                        Map<String, Object> map = new HashMap<>();
                        map.put("token", token);
                        databaseReference.updateChildren(map);
                        if(loadingDialog != null)
                        loadingDialog.DismissDialog();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    });

        });
    }
}
