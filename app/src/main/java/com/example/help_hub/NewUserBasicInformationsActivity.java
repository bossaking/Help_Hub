package com.example.help_hub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewUserBasicInformationsActivity extends AppCompatActivity implements TextWatcher {

    EditText mUserName, mUserPhoneNumber, mUserCity;
    Button mUserSaveButton;


    LoadingDialog loadingDialog;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    Drawable defaultBackground;

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

        mUserName = findViewById(R.id.new_user_name);
        mUserPhoneNumber = findViewById(R.id.new_user_phone_number);
        mUserCity = findViewById(R.id.new_user_city);
        mUserSaveButton = findViewById(R.id.new_user_save_button);

        defaultBackground = mUserName.getBackground();
        mUserName.addTextChangedListener(this);
        mUserPhoneNumber.addTextChangedListener(this);
        mUserCity.addTextChangedListener(this);

        mUserSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Registration(email, password);

            }
        });
    }

    private boolean ValidateUserName(String userName){

        if(userName.isEmpty()){
            mUserName.setBackgroundResource(R.drawable.edit_error_border);
            mUserName.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }
    private boolean ValidatePhoneNumber(String phoneNumber){

        if(phoneNumber.isEmpty()){
            mUserPhoneNumber.setBackgroundResource(R.drawable.edit_error_border);
            mUserPhoneNumber.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }
    private boolean ValidateCity(String city){

        if(city.isEmpty()){
            mUserCity.setBackgroundResource(R.drawable.edit_error_border);
            mUserCity.setError(getString(R.string.empty_field_error));
            return false;
        }

        return true;
    }

    private void Registration(String email, String password){

        final String name = mUserName.getText().toString().trim();
        final String phoneNumber = mUserPhoneNumber.getText().toString().trim();
        final String city = mUserCity.getText().toString().trim();

        if(!ValidateUserName(name) || !ValidatePhoneNumber(phoneNumber) || !ValidateCity(city)){
            return;
        }

        loadingDialog.StartLoadingDialog();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            //Jeżeli udało się
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(NewUserBasicInformationsActivity.this, "User created", Toast.LENGTH_SHORT).show();
                    SaveUserData(name, phoneNumber, city);
                }else{
                    Toast.makeText(NewUserBasicInformationsActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    loadingDialog.DismissDialog();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {

            //Jeżeli się nie udało
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewUserBasicInformationsActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                loadingDialog.DismissDialog();
            }
        });

    }

    private void SaveUserData(String name, String phoneNumber, String city){

        String userId = firebaseAuth.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Name", name);
        userMap.put("Phone number", phoneNumber);
        userMap.put("City", city);

        documentReference.set(userMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());


    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mUserName.setBackground(defaultBackground);
        mUserPhoneNumber.setBackground(defaultBackground);
        mUserCity.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}