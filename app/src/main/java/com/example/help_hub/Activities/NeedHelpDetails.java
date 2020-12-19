package com.example.help_hub.Activities;

import android.net.Uri;
import android.view.View;
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

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.help_hub.Adapters.SliderAdapter;
import com.example.help_hub.Fragments.NeedHelpFragment;
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    private TextView userNameTextView;
    private TextView phoneNumberTextView;

    private ViewPager2 pager2;

    Context myContext;

    String userId, announcementId;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_need_help_details);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        myContext = getApplicationContext();

        //needHelpImage = findViewById(R.id.need_help_photos);
        titleTextView = findViewById(R.id.need_help_title);
        priceTextView = findViewById(R.id.need_help_budget);
        descriptionTextView = findViewById(R.id.need_help_description);
        needHelpUserImage = findViewById(R.id.need_help_user_photo);

        userNameTextView = findViewById(R.id.need_help_user_name);
        phoneNumberTextView = findViewById(R.id.need_help_user_phone_number);

        Bundle bundle = getIntent().getExtras();

        titleTextView.setText(bundle.getString(EXTRA_NEED_HELP_TITLE));
        priceTextView.setText(bundle.getString(EXTRA_NEED_HELP_PRICE) + " " + getString(R.string.new_notice_currency));
        descriptionTextView.setText(bundle.getString(EXTRA_NEED_HELP_DESCRIPTION));

        userId = bundle.getString(EXTRA_NEED_HELP_USER_ID);
        announcementId = bundle.getString(EXTRA_NEED_HELP_ID);

        pager2 = findViewById(R.id.need_help_photos);
        List<Uri> imagesUri = new ArrayList<>();

        sliderAdapter = new SliderAdapter(imagesUri, pager2, getApplicationContext());

        StorageReference imgRef = storageReference.child("announcement/" + announcementId + "/images");
        imgRef.listAll().addOnSuccessListener(listResult -> {
           for(StorageReference fileRef : listResult.getItems()){
               fileRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                   String name = storageMetadata.getName();
                   fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                       imagesUri.add(uri);
                       sliderAdapter.notifyDataSetChanged();
                   }).addOnCompleteListener(task -> {
                   });
               });
           }
           pager2.setAdapter(sliderAdapter);

        });

        StorageReference imgUserRef = storageReference.child("users/" + userId + "/profile.jpg");
        imgUserRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(needHelpUserImage);
        }).addOnFailureListener(v -> {
            needHelpUserImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
        });

        DocumentReference reference = firebaseFirestore.collection("users").document(userId);
        Task<DocumentSnapshot> userSnap = reference.get();
        userSnap.addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               DocumentSnapshot doc = task.getResult();
               userNameTextView.setText(doc.getString("Name"));
               phoneNumberTextView.setText(doc.getString("Phone number"));
           }
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