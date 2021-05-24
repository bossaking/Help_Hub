package com.example.help_hub.Activities;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.example.help_hub.OtherClasses.MyApplication;
import com.example.help_hub.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        calculateUnreadConversations();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert fragment != null;
        fragment.getChildFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
    }

    public void calculateUnreadConversations() {

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("chats")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            int unreadConversations = 0;
            for (DocumentSnapshot ds : queryDocumentSnapshots) {

                if (ds.contains("has unread messages")) {
                    if (ds.getBoolean("has unread messages")) {
                        unreadConversations++;
                    }
                }
            }
            if(unreadConversations > 0){
                BadgeDrawable badgeDrawable = ((BottomNavigationView)findViewById(R.id.nav_view)).getOrCreateBadge(R.id.messages);
                badgeDrawable.setNumber(unreadConversations);
            }else{
                ((BottomNavigationView)findViewById(R.id.nav_view)).removeBadge(R.id.messages);
            }
        });

    }

    public void updateUnreadConversationsNumber(){
        BadgeDrawable badgeDrawable = ((BottomNavigationView)findViewById(R.id.nav_view)).getOrCreateBadge(R.id.messages);
        badgeDrawable.setNumber(badgeDrawable.getNumber() - 1);
        if(badgeDrawable.getNumber() <= 0){
            ((BottomNavigationView)findViewById(R.id.nav_view)).removeBadge(R.id.messages);
        }
    }

    @Override
    protected void onResume() {
        MyApplication.getInstance().setCurrentActivity(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MyApplication.getInstance().setCurrentActivity(null);
        super.onPause();
    }
}