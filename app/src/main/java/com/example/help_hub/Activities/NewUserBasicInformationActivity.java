package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewUserBasicInformationActivity extends AppCompatActivity implements TextWatcher {

    private EditText userName, userPhoneNumber, userCity;
    private Button saveButton;
    private Drawable defaultBackground;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_basic_informations);

        Bundle extras = getIntent().getExtras();
        final String email = extras.getString("USER_EMAIL");
        final String password = extras.getString("USER_PASSWORD");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadingDialog = new LoadingDialog(this);

        userName = findViewById(R.id.new_user_name);
        userPhoneNumber = findViewById(R.id.new_user_phone_number);
        userCity = findViewById(R.id.new_user_city);
        saveButton = findViewById(R.id.new_user_save_button);

        defaultBackground = userName.getBackground();
        userName.addTextChangedListener(this);
        userPhoneNumber.addTextChangedListener(this);
        userCity.addTextChangedListener(this);

        saveButton.setOnClickListener(v -> userRegistration(email, password));
    }

    private boolean validateUserName(String mUserName) {
        if (mUserName.isEmpty()) {
            userName.setBackgroundResource(R.drawable.edit_error_border);
            userName.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            userPhoneNumber.setBackgroundResource(R.drawable.edit_error_border);
            userPhoneNumber.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }

    private boolean validateCity(String city) {
        if (city.isEmpty()) {
            userCity.setBackgroundResource(R.drawable.edit_error_border);
            userCity.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }

    private void userRegistration(String email, String password) {
        final String name = userName.getText().toString().trim();
        final String phoneNumber = userPhoneNumber.getText().toString().trim();
        final String city = userCity.getText().toString().trim();

        if (!validateUserName(name) || !validatePhoneNumber(phoneNumber) || !validateCity(city))
            return;

        loadingDialog.StartLoadingDialog();

        //Jeżeli udało się
        //Jeżeli się nie udało
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewUserBasicInformationActivity.this, R.string.User_created, Toast.LENGTH_SHORT).show();
                        saveUserData(name, phoneNumber, city);
                    } else {
                        Toast.makeText(NewUserBasicInformationActivity.this, getString(R.string.error) + task.getException(), Toast.LENGTH_SHORT).show();
                        loadingDialog.DismissDialog();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewUserBasicInformationActivity.this, R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    loadingDialog.DismissDialog();
                });
    }

    private void saveUserData(String name, String phoneNumber, String city) {
        String userId = firebaseAuth.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Name", name);
        userMap.put("Phone number", phoneNumber);
        userMap.put("City", city);
        userMap.put("Role", "User");

        documentReference.set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        userName.setBackground(defaultBackground);
        userPhoneNumber.setBackground(defaultBackground);
        userCity.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}