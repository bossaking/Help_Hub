package com.example.help_hub.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.AlertDialogues.SelectTypeOfAdvertisement;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    UserDatabase userDatabase;
    BottomNavigationView navView;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        if(UserDatabase.instance == null){
            userDatabase = UserDatabase.getInstance(this);
            userDatabase.profileDataLoaded = this::CheckRole;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        fragment.getChildFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
    }

    public void CheckRole(){
        navView = findViewById(R.id.nav_view);
        if(userDatabase.getUser().getRole() != null){
            if(userDatabase.getUser().getRole().equals("Administrator")) {
                navView.getMenu().findItem(R.id.administration).setVisible(true);
            }
        }else{
            userDatabase.getUser().setRole("User");
        }

        loadingDialog.DismissDialog();
    }
}