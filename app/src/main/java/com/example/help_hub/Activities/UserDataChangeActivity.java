package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.AlertDialogues.ChangePasswordDialog;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserDataChangeActivity extends AppCompatActivity {

    private EditText mChangedDescription, mChangedName, mChangedPhoneNumber, mChangedCity;
    private Button changePassword, applyChanges;

    private User user;
    private UserDatabase userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_change);

        userDatabase = UserDatabase.getInstance(this, FirebaseAuth.getInstance().getUid());
        user = userDatabase.getUser();

        changePassword = findViewById(R.id.user_data_change_new_password_button);
        applyChanges = findViewById(R.id.user_data_changes_apply);

        mChangedDescription = findViewById(R.id.changed_portfolio_description);
        mChangedName = findViewById(R.id.changed_name);
        mChangedCity = findViewById(R.id.changed_city);
        mChangedPhoneNumber = findViewById(R.id.changed_phoneNumber);

        mChangedDescription.setText(user.getDescription());
        mChangedName.setText(user.getName());
        mChangedCity.setText(user.getCity());
        mChangedPhoneNumber.setText(user.getPhoneNumber());

        changePassword.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(UserDataChangeActivity.this);
            changePasswordDialog.StartChangePasswordDialog();
        });

        applyChanges.setOnClickListener(v -> {
            user.setDescription(mChangedDescription.getText().toString());
            user.setName(mChangedName.getText().toString());
            user.setPhoneNumber(mChangedPhoneNumber.getText().toString());
            user.setCity(mChangedCity.getText().toString());
            updateUserDataFirebase();
        });
    }

    public void updateUserDataFirebase() {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(user.getId());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Name", user.getName());
        userMap.put("Phone number", user.getPhoneNumber());
        userMap.put("City", user.getCity());
        userMap.put("Description", user.getDescription());

        documentReference.update(userMap)
                .addOnCompleteListener(task -> {
                    Toast.makeText(getApplicationContext(), R.string.Updated, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }
}