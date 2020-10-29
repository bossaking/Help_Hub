package com.example.help_hub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    Button editButton;

    TextView mUserName, mUserPhoneNumber, mUserCity;

    private ImageView profileImage;
    private static final int PICK_IMAGE = 1;
    Uri imageUri;

    LoadingDialog loadingDialog;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        profileImage = (ImageView) findViewById(R.id.Profile_Image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
            }
        });

        editButton = findViewById(R.id.user_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),UserDataChange.class);
                startActivity(intent);
            }
        });

        mUserPhoneNumber = findViewById(R.id.user_phone_number);
        mUserName = findViewById(R.id.user_name);
        mUserCity = findViewById(R.id.user_city);

        GetUserInformation();

    }

    //Zmiana zdjęcia
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void GetUserInformation(){

        String userId = firebaseAuth.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mUserName.setText(documentSnapshot.getString("Name"));
                mUserPhoneNumber.setText(documentSnapshot.getString("Phone number"));
                mUserCity.setText(documentSnapshot.getString("City"));
                loadingDialog.DismissDialog();
            }
        });

    }
}