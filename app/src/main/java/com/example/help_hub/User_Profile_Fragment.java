package com.example.help_hub;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.borjabravo.readmoretextview.ReadMoreTextView;
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
    TextView mUserName, mUserPhoneNumber, mUserCity, showAllPortfolioPhotos, mUserPortfolioDescription;
    LinearLayout firstImagesLayout;

    ImageView profileImage;

    LoadingDialog dataLoadingDialog;
    LoadingDialog imageLoadingDialog;

    UserActivity userActivity;
    Database database;
    UserDatabase userDatabase;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        userActivity = (UserActivity)getActivity();

        dataLoadingDialog = new LoadingDialog(getActivity());
        imageLoadingDialog = new LoadingDialog(getActivity());
        dataLoadingDialog.StartLoadingDialog();
        imageLoadingDialog.StartLoadingDialog();

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
        mUserPortfolioDescription = view.findViewById(R.id.portfolio_description);



        if(Database.instance == null){
            database = Database.getInstance(userActivity);
            database.arrayChangedListener = this::LoadUserPortfolioPhotos;
            database.Initialize();
        }else{
            database = Database.getInstance(userActivity);
            LoadUserPortfolioPhotos();
        }

        if(UserDatabase.instance == null){
            userDatabase = UserDatabase.getInstance(userActivity);
            userDatabase.profileDataLoaded = this::GetUserInformation;
            userDatabase.profileImageLoaded = this::SetProfileImage;
        }else{
            userDatabase = UserDatabase.getInstance(userActivity);
            GetUserInformation();
            SetProfileImage(userDatabase.getUser().getProfileImage());
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        imageLoadingDialog.StartLoadingDialog();

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                SetProfileImage(resultUri);
                userDatabase.SetUserProfileImage(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(), "Error: " + result.getError(), Toast.LENGTH_LONG).show();
                imageLoadingDialog.DismissDialog();
            } else {
                imageLoadingDialog.DismissDialog();
            }
        } else if (requestCode == 100 && resultCode == RESULT_OK) {

            LoadUserPortfolioPhotos();
            imageLoadingDialog.DismissDialog();

        } else {
            imageLoadingDialog.DismissDialog();
        }
    }

    private void LoadUserPortfolioPhotos() {

        ImageView imageView = null;
        firstImagesLayout.removeAllViews();

        for (int i = 0; i < database.GetPortfolioImagesCount() && i < 3; i++) {

            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getContext()).inflate(R.layout.portfolio_image_card, null);
            imageView = view.findViewById(R.id.portfolio_image);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 300, 3f);
            layoutParams.setMargins(0, 10, 5, 5);
            view.setLayoutParams(layoutParams);
            firstImagesLayout.addView(view);
            Glide.with(getActivity()).load(database.GetImage(i).getImageUri()).placeholder(R.drawable.base_image_24).into(imageView);
            int finalI = i;
            imageView.setOnLongClickListener(c -> {
                DeletePortfolioImage(finalI);
                return true;
            });
        }

        if (database.GetPortfolioImagesCount() > 3) {

            showAllPortfolioPhotos.setVisibility(View.VISIBLE);

        } else {
            showAllPortfolioPhotos.setVisibility(View.GONE);
            imageView.setOnClickListener(v -> {
                Intent intent;

                try {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                }
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                getActivity().startActivityForResult(intent, 100);
            });

            imageView.setOnLongClickListener(v -> true);
        }

    }

    private void GetUserInformation() {

        User user = userDatabase.getUser();

        mUserName.setText(user.getName());
        mUserPhoneNumber.setText(user.getPhoneNumber());
        mUserCity.setText(user.getCity());
        mUserPortfolioDescription.setText(user.getDescription() == null ? "" : user.getDescription());
        if (mUserPortfolioDescription.getText().toString().isEmpty()) {
            mUserPortfolioDescription.setVisibility(View.GONE);
        } else {
            mUserPortfolioDescription.setVisibility(View.VISIBLE);
        }
        dataLoadingDialog.DismissDialog();
    }

    private void SetProfileImage(Uri imageUri) {
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.default_user_image).into(profileImage);
        imageLoadingDialog.DismissDialog();
    }

    private void DeletePortfolioImage(int position) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c -> {
            database.DeletePortfolioImageFromFirebase(database.GetImage(position));
            LoadUserPortfolioPhotos();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }
}
