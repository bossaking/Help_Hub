package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Fragments.NeedHelpFragment;
import com.example.help_hub.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class NeedHelpDetails extends AppCompatActivity {

    public static final String EXTRA_NEED_HELP_ID = "NEED_HELP_ID";
    public static final String EXTRA_NEED_HELP_TITLE = "NEED_HELP_TITLE";
    public static final String EXTRA_NEED_HELP_PRICE = "NEED_HELP_PRICE";
    public static final String EXTRA_NEED_HELP_DESCRIPTION = "NEED_HELP_DESCRIPTION";
    public static final String EXTRA_NEED_HELP_USER_ID = "NEED_HELP_USER_ID";

    private ImageView needHelpImage;
    private ImageView needHelpUserImage;

    private TextView titleTextView;
    private TextView priceTextView;
    private TextView descriptionTextView;

    Context myContext;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_need_help_details);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();

        myContext = getApplicationContext();

        needHelpImage = findViewById(R.id.need_help_photos);
        titleTextView = findViewById(R.id.need_help_title);
        priceTextView = findViewById(R.id.need_help_budget);
        descriptionTextView = findViewById(R.id.need_help_description);
        needHelpUserImage = findViewById(R.id.need_help_user_photo);

        Bundle bundle = getIntent().getExtras();

        titleTextView.setText(bundle.getString(EXTRA_NEED_HELP_TITLE));
        priceTextView.setText(bundle.getString(EXTRA_NEED_HELP_PRICE) + " " + getString(R.string.new_notice_currency));
        descriptionTextView.setText(bundle.getString(EXTRA_NEED_HELP_DESCRIPTION));

        StorageReference imgRef = storageReference.child("announcement/" + bundle.getString(EXTRA_NEED_HELP_ID) + "/images/photo0");
        imgRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).into(needHelpImage);
        }).addOnFailureListener(v -> {
            needHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
        });

        StorageReference imgUserRef = storageReference.child("users/" + bundle.getString(EXTRA_NEED_HELP_USER_ID) + "/profile.jpg");
        imgUserRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).into(needHelpUserImage);
        }).addOnFailureListener(v -> {
            needHelpUserImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
        });
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
}