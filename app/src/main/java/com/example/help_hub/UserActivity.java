package com.example.help_hub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {


    FragmentManager fragmentManager;
    Fragment userFragment;

    List<Uri> userPortfolioPhotos;
    ClipData clipData;

    User_Profile_Fragment user_profile_fragment;
    User_Portfolio_Photos_Fragment user_portfolio_photos_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        fragmentManager = getSupportFragmentManager();

        ShowUserProfile();

        userPortfolioPhotos = new ArrayList<>();
        userPortfolioPhotos.add(Uri.parse("android.resource://" + getPackageName() + "/drawable/add_a_photo_24"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            ClipData clipData = data.getClipData();
            if(clipData != null){
                for(int i = 0; i < clipData.getItemCount(); i++){
                    userPortfolioPhotos.add(userPortfolioPhotos.size() - 1, clipData.getItemAt(i).getUri());
                }
            }
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.user_fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    public void ShowAllPortfolioPhotos(){
        if(user_portfolio_photos_fragment == null){
            user_portfolio_photos_fragment = new User_Portfolio_Photos_Fragment();
        }
        fragmentManager.beginTransaction().replace(R.id.user_fragment_container, user_portfolio_photos_fragment).commit();
    }

    public void ShowUserProfile(){
        if(userFragment == null){
            userFragment = new User_Profile_Fragment();
        }
        fragmentManager.beginTransaction().replace(R.id.user_fragment_container, userFragment).commit();
    }

    @Override
    public void onBackPressed() {

        if(fragmentManager.findFragmentById(R.id.user_fragment_container) instanceof User_Portfolio_Photos_Fragment){
            ShowUserProfile();
        }else{
            super.onBackPressed();
        }
    }
}