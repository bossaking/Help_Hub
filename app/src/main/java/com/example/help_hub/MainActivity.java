package com.example.help_hub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button logoutButton;
    TextView userIdText;
    Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutButton = findViewById(R.id.logout_button);
        userIdText = findViewById(R.id.user_id_text);
        profileButton = findViewById(R.id.profile_button);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userIdText.setText(firebaseAuth.getUid());

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            UserDatabase.ClearInstance();
            Database.ClearInstance();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),UserActivity.class);
            startActivity(intent);
        });
    }


}
