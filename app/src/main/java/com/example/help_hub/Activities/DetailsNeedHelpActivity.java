package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.help_hub.Fragments.NeedHelpDetailsFragment;

import com.example.help_hub.R;

public class DetailsNeedHelpActivity extends AppCompatActivity {

    public static final String EXTRA_NEED_HELP_ID = "NEED_HELP_ID",
            EXTRA_NEED_HELP_TITLE = "NEED_HELP_TITLE",
            EXTRA_NEED_HELP_PRICE = "NEED_HELP_PRICE",
            EXTRA_NEED_HELP_DESCRIPTION = "NEED_HELP_DESCRIPTION",
            EXTRA_NEED_HELP_USER_ID = "NEED_HELP_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_help_details);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment detailsFragment = new NeedHelpDetailsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.need_help_details_container, detailsFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }
}