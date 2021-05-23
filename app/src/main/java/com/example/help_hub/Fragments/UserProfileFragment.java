package com.example.help_hub.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.help_hub.*;
import com.example.help_hub.Activities.*;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.Singletones.UserDatabase;
import com.example.help_hub.Singletones.UserPortfolioImagesDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment {

    private TextView mUserName, mUserPhoneNumber, mUserCity, showAllPortfolioPhotos,
            mUserPortfolioDescription, logoutButton, opinionsCountTextView;
    private ImageView profileImage;
    private RatingBar ratingBar;

    private LoadingDialog dataLoadingDialog;
    private LoadingDialog imageLoadingDialog;

    private LinearLayout firstImagesLayout;

    private UserDatabase userDatabase;
    public UserPortfolioImagesDatabase userPortfolioImagesDatabase;

    private Activity myActivity;
    private Context myContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        myActivity = getActivity();

        setHasOptionsMenu(true);

        dataLoadingDialog = new LoadingDialog(getActivity());
        imageLoadingDialog = new LoadingDialog(getActivity());
        dataLoadingDialog.StartLoadingDialog();
        imageLoadingDialog.StartLoadingDialog();

        firstImagesLayout = view.findViewById(R.id.first_images_layout);

        showAllPortfolioPhotos = view.findViewById(R.id.show_all_photos);
        showAllPortfolioPhotos.setOnClickListener(c -> ShowAllPortfolioPhotos());

        profileImage = view.findViewById(R.id.Profile_Image);
        profileImage.setOnClickListener(view1 -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.OVAL)
                .start(getActivity()));

        logoutButton = view.findViewById(R.id.user_logout_button);
        logoutButton.setOnClickListener(v -> {
            try {
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid()).removeValue();
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FirebaseAuth.getInstance().signOut();
            UserDatabase.clearInstance();
            UserPortfolioImagesDatabase.clearInstance();
            startActivity(new Intent(myContext, LoginActivity.class));
            myActivity.finish();
        });

        mUserPhoneNumber = view.findViewById(R.id.user_phone_number);
        mUserName = view.findViewById(R.id.user_name);
        mUserCity = view.findViewById(R.id.user_city);
        mUserPortfolioDescription = view.findViewById(R.id.portfolio_description);
        ratingBar = view.findViewById(R.id.rating_bar);
        opinionsCountTextView = view.findViewById(R.id.opinions_count_text_view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.user_menu_edit) {
            Intent intent = new Intent(getContext(), UserDataChangeActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myActivity = getActivity();
        myContext = myActivity.getApplicationContext();

        Toolbar myToolbar = myActivity.findViewById(R.id.my_toolbar);
        ((AppCompatActivity) myActivity).setSupportActionBar(myToolbar);
        ((AppCompatActivity) myActivity).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void ShowAllPortfolioPhotos() {
        Intent intent = new Intent(getContext(), UserPortfolioPhotosActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        userPortfolioImagesDatabase = UserPortfolioImagesDatabase.getInstance(myActivity);

        imageLoadingDialog.StartLoadingDialog();

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {

                    userPortfolioImagesDatabase.imagesCount = clipData.getItemCount() + 1;

                    PortfolioImage portfolioImage = new PortfolioImage(DocumentFile.fromSingleUri(myContext,
                            clipData.getItemAt(i).getUri()).getName(), clipData.getItemAt(i).getUri());

                    userPortfolioImagesDatabase.AddNewImage(portfolioImage);
                    userPortfolioImagesDatabase.LoadPortfolioImageToDatabase(portfolioImage);

                }
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //SetProfileImage(resultUri);
                userDatabase.SetUserProfileImage(resultUri);
                userDatabase.profileImageChanged = uri -> {
                    Glide.with(getActivity()).load(uri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).into(profileImage);
                };
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getContext(), getString(R.string.error) + result.getError(), Toast.LENGTH_LONG).show();
                imageLoadingDialog.DismissDialog();
            } else imageLoadingDialog.DismissDialog();
        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            loadUserPortfolioPhotos();
            imageLoadingDialog.DismissDialog();
        } else imageLoadingDialog.DismissDialog();
    }

    @Override
    public void onResume() {
        super.onResume();

        userPortfolioImagesDatabase = UserPortfolioImagesDatabase.getInstance(myActivity);
        userPortfolioImagesDatabase.arrayChangedListener = this::loadUserPortfolioPhotos;
        userPortfolioImagesDatabase.Initialize(FirebaseAuth.getInstance().getUid());

        userDatabase = UserDatabase.getInstance(myActivity, FirebaseAuth.getInstance().getUid());
        userDatabase.getUserFromFirebase(FirebaseAuth.getInstance().getUid());
        userDatabase.profileDataLoaded = this::getUserInformation;
        userDatabase.profileImageLoaded = this::setProfileImage;
    }

    private void LoadUserPortfolioPhotos() {

        if(userPortfolioImagesDatabase.imagesCount != userPortfolioImagesDatabase.GetPortfolioImagesCount()) return;

        ImageView imageView = null;
        firstImagesLayout.removeAllViews();

        for (int i = 0; i < userPortfolioImagesDatabase.GetPortfolioImagesCount() && i < 3; i++) {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getContext()).inflate(R.layout.portfolio_image_card, null);
            imageView = view.findViewById(R.id.portfolio_image);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 300, 3f);
            layoutParams.setMargins(0, 10, 5, 5);

            view.setLayoutParams(layoutParams);
            firstImagesLayout.addView(view);

            Glide.with(getActivity()).load(userPortfolioImagesDatabase.GetImage(i).getImageUri()).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24)
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).into(imageView);
            int finalI = i;
            imageView.setOnLongClickListener(c -> {
                deletePortfolioImage(finalI);
                return true;
            });
        }

        if (userPortfolioImagesDatabase.GetPortfolioImagesCount() > 3)
            showAllPortfolioPhotos.setVisibility(View.VISIBLE);
        else {
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

    private void getUserInformation() {
        try {
            User user = userDatabase.getUser();

            mUserName.setText(user.getName());
            mUserPhoneNumber.setText(user.getPhoneNumber());
            mUserCity.setText(user.getCity());
            mUserPortfolioDescription.setText(user.getDescription() == null ? "" : user.getDescription());

            if (mUserPortfolioDescription.getText().toString().isEmpty())
                mUserPortfolioDescription.setVisibility(View.GONE);
            else mUserPortfolioDescription.setVisibility(View.VISIBLE);

            if (user.getUserRating() == 0) {
                ratingBar.setVisibility(View.GONE);
                opinionsCountTextView.setVisibility(View.GONE);
            } else {
                ratingBar.setRating(user.getUserRating());
                opinionsCountTextView.setText("(" + (int) user.getAllOpinionsCount() + " " + getString(R.string.opinions) + ")");
                opinionsCountTextView.setOnClickListener(v -> showAllOpinions(user));
            }
        } catch (Exception e) {
        }

        dataLoadingDialog.DismissDialog();
    }

    private void showAllOpinions(User user) {
        Intent intent = new Intent(myContext, AllOpinionsActivity.class);
        intent.putExtra(AllOpinionsActivity.USER_ID, user.getId());
        intent.putExtra(AllOpinionsActivity.USER_NAME, user.getName());
        myActivity.startActivity(intent);
    }

    private void SetProfileImage(Uri imageUri) {

        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).into(profileImage);

        imageLoadingDialog.DismissDialog();
    }


    private void deletePortfolioImage(int position) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c -> {
            userPortfolioImagesDatabase.DeletePortfolioImageFromFirebase(userPortfolioImagesDatabase.GetImage(position));
            userPortfolioImagesDatabase.imagesCount--;
            LoadUserPortfolioPhotos();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }
}