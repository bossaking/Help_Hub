package com.example.help_hub;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

public class User_Profile_Fragment extends Fragment {

    Button editButton;
    TextView mUserName, mUserPhoneNumber, mUserCity, showAllPortfolioPhotos;
    LinearLayout firstImagesLayout;


    ImageView profileImage;

    LoadingDialog loadingDialog;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    String userId;

    UserActivity userActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.StartLoadingDialog();


        firstImagesLayout = view.findViewById(R.id.first_images_layout);

        showAllPortfolioPhotos = view.findViewById(R.id.show_all_photos);
        showAllPortfolioPhotos.setOnClickListener(c -> {
            userActivity.ShowAllPortfolioPhotos();
        });

        profileImage = view.findViewById(R.id.Profile_Image);
        profileImage.setOnClickListener(view1 -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.OVAL)
                .start(getActivity()));

        editButton = view.findViewById(R.id.user_edit_button);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserDataChange.class);
            startActivity(intent);
        });

        mUserPhoneNumber = view.findViewById(R.id.user_phone_number);
        mUserName = view.findViewById(R.id.user_name);
        mUserCity = view.findViewById(R.id.user_city);
        userId = firebaseAuth.getUid();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userActivity = (UserActivity)getActivity();
        GetUserInformation();
        LoadUserPortfolioPhotos();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        loadingDialog.StartLoadingDialog();

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                SetProfileImage(resultUri);
                UploadImageToDatabase(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(), "Error: " + result.getError(), Toast.LENGTH_LONG).show();
                loadingDialog.DismissDialog();
            } else {
                loadingDialog.DismissDialog();
            }
        }else if(requestCode == 100 && resultCode == RESULT_OK){

                LoadUserPortfolioPhotos();

                loadingDialog.DismissDialog();

        }else{
            loadingDialog.DismissDialog();
        }
    }

    private void LoadUserPortfolioPhotos(){

        ImageView imageView = null;
        firstImagesLayout.removeAllViews();

        for(int i = 0; i < userActivity.userPortfolioPhotos.size() && i < 3; i++){
            imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300, 3f);
            layoutParams.setMargins(5,10,5,5);
            imageView.setLayoutParams(layoutParams);
            imageView.setVisibility(View.VISIBLE);
            firstImagesLayout.addView(imageView);
            Glide.with(getActivity()).load(userActivity.userPortfolioPhotos.get(i)).placeholder(R.drawable.base_image_24).into(imageView);
            int finalI = i;
            imageView.setOnLongClickListener(c -> {
                DeleteProfileImage(finalI);
                return true;
            });
        }

        if(userActivity.userPortfolioPhotos.size() > 3){

            showAllPortfolioPhotos.setVisibility(View.VISIBLE);

        }else{
            imageView.setOnClickListener(v -> {
                Intent intent;

                try{
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }catch (Exception e){
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                }
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                getActivity().startActivityForResult(intent, 100);
            });

            imageView.setOnLongClickListener(v ->  true);
        }

    }

    private void GetUserInformation() {

        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        final ListenerRegistration registration = documentReference.addSnapshotListener((documentSnapshot, e) -> {
            mUserName.setText(documentSnapshot.getString("Name"));
            mUserPhoneNumber.setText(documentSnapshot.getString("Phone number"));
            mUserCity.setText(documentSnapshot.getString("City"));
        });

        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            SetProfileImage(uri);
            loadingDialog.DismissDialog();
            registration.remove();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            loadingDialog.DismissDialog();
        });

    }

    private void SetProfileImage(Uri imageUri) {
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.default_user_image).into(profileImage);
    }

    private void UploadImageToDatabase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("users/" + userId + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Photo has been loaded", Toast.LENGTH_SHORT).show();
            loadingDialog.DismissDialog();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            loadingDialog.DismissDialog();
        });
    }

    private void DeleteProfileImage(int position){
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c->{
            userActivity.userPortfolioPhotos.remove(position);
            LoadUserPortfolioPhotos();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }

}
