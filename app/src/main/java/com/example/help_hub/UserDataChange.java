package com.example.help_hub;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserDataChange extends AppCompatActivity {

    private Button changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_change);

        changePassword = findViewById(R.id.user_data_change_new_password_button);

        changePassword.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(UserDataChange.this);
            changePasswordDialog.StartChangePasswordDialog();
        });
    }
}