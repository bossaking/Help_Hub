package com.example.help_hub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserDataChange extends AppCompatActivity {

    private Button changePassword, applyChanges;
    EditText mChangedDescription, mChangedName, mChangedPhoneNumber, mChangedCity;

    User user;
    UserDatabase userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_change);

        userDatabase = UserDatabase.getInstance(this);
        user = userDatabase.getUser();

        changePassword = findViewById(R.id.user_data_change_new_password_button);
        applyChanges = findViewById(R.id.user_data_changes_apply);

        mChangedDescription = findViewById(R.id.changed_portfolio_description);
        mChangedDescription.setText(user.getDescription());

        mChangedName = findViewById(R.id.changed_name);
        mChangedName.setText(user.getName());

        mChangedCity = findViewById(R.id.changed_city);
        mChangedCity.setText(user.getCity());

        mChangedPhoneNumber = findViewById(R.id.changed_phoneNumber);
        mChangedPhoneNumber.setText(user.getPhoneNumber());

        changePassword.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(UserDataChange.this);
            changePasswordDialog.StartChangePasswordDialog();
        });

        applyChanges.setOnClickListener(v -> {
            user.setDescription(mChangedDescription.getText().toString());
            user.setName(mChangedName.getText().toString());
            user.setPhoneNumber(mChangedPhoneNumber.getText().toString());
            user.setCity(mChangedCity.getText().toString());
            UpdateUserDataFirebase();
        });
    }

    public void UpdateUserDataFirebase(){

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(user.getId());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Name", user.getName());
        userMap.put("Phone number", user.getPhoneNumber());
        userMap.put("City", user.getCity());
        userMap.put("Description", user.getDescription());

        documentReference.update(userMap).addOnCompleteListener(task -> {
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());

    }
}