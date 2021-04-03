package com.example.help_hub.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.*;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.User;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserDatabase;
import com.example.help_hub.Singletones.UserPortfolioImagesDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public class OtherUserProfileFragment extends Fragment {

    FragmentManager fragmentManager;

    Activity myActivity;
    Context myContext;

    public UserPortfolioImagesDatabase userPortfolioImagesDatabase;

    TextView mUserName, mUserPhoneNumber, mUserCity, mUserPortfolioDescription;
    LinearLayout firstImagesLayout;

    ImageView profileImage;

    LoadingDialog dataLoadingDialog;
    LoadingDialog imageLoadingDialog;

    UserDatabase userDatabase;

    private boolean inBookmarks = false;

    String userId;

    private RatingBar ratingBar;

    public OtherUserProfileFragment(String userId) {
        this.userId = userId;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);
        myActivity = getActivity();

        setHasOptionsMenu(true);

        dataLoadingDialog = new LoadingDialog(myActivity);
        imageLoadingDialog = new LoadingDialog(myActivity);
        dataLoadingDialog.StartLoadingDialog();
        imageLoadingDialog.StartLoadingDialog();

        firstImagesLayout = view.findViewById(R.id.first_images_layout);

        profileImage = view.findViewById(R.id.Profile_Image);

        mUserPhoneNumber = view.findViewById(R.id.user_phone_number);
        mUserName = view.findViewById(R.id.user_name);
        mUserCity = view.findViewById(R.id.user_city);
        mUserPortfolioDescription = view.findViewById(R.id.portfolio_description);

        ratingBar = view.findViewById(R.id.rating_bar);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.other_user_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        if(userId.equals(FirebaseAuth.getInstance().getUid())){
            menu.findItem(R.id.add_to_bookmark_button).setVisible(false);
        }else{
            checkBookmarks(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.add_to_bookmark_button) {

            if (inBookmarks) {
                removeUserFromBookmarks();
                item.setIcon(R.drawable.ic_baseline_star_border_24);


            } else {
                addUserToBookmarks();

                item.setIcon(R.drawable.ic_baseline_star_24);
            }

            inBookmarks = !inBookmarks;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myActivity = getActivity();
        myContext = myActivity.getApplicationContext();

        fragmentManager = getChildFragmentManager();


    }

    @Override
    public void onResume() {
        super.onResume();


        userPortfolioImagesDatabase = UserPortfolioImagesDatabase.getInstance(myActivity);
        userPortfolioImagesDatabase.arrayChangedListener = this::LoadUserPortfolioPhotos;
        userPortfolioImagesDatabase.Initialize(userId);


        userDatabase = UserDatabase.getInstance(myActivity, userId);
        userDatabase.getUserFromFirebase(userId);
        userDatabase.profileDataLoaded = this::GetUserInformation;
        userDatabase.profileImageLoaded = this::SetProfileImage;


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

        ratingBar.setRating(user.getUserRating());

        dataLoadingDialog.DismissDialog();
    }

    private void SetProfileImage(Uri imageUri) {
        Glide.with(myActivity).load(imageUri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(profileImage);
        imageLoadingDialog.DismissDialog();
    }

    private void LoadUserPortfolioPhotos() {

        ImageView imageView = null;
        firstImagesLayout.removeAllViews();

        for (int i = 0; i < userPortfolioImagesDatabase.GetPortfolioImagesCount() - 1; i++) {

            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getContext()).inflate(R.layout.portfolio_image_card, null);
            imageView = view.findViewById(R.id.portfolio_image);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 300, 3f);
            layoutParams.setMargins(0, 10, 5, 5);
            view.setLayoutParams(layoutParams);
            firstImagesLayout.addView(view);
            Glide.with(getActivity()).load(userPortfolioImagesDatabase.GetImage(i).getImageUri()).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24)
                    .into(imageView);
        }
    }

    private void addUserToBookmarks() {
        HashMap<String, Object> user = new HashMap<>();
        user.put("user id", userId);
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("bookmarks users").document(userId).set(user).addOnSuccessListener(unused -> {
            Snackbar.make(getView(), getString(R.string.added_to_bookmarks), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void removeUserFromBookmarks() {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("bookmarks users").document(userId).delete().addOnSuccessListener(unused ->
                Snackbar.make(getView(), getString(R.string.removed_from_bookmarks), Snackbar.LENGTH_SHORT).show()
        );
    }

    private void checkBookmarks(Menu menu) {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("bookmarks users").document(userId).get().addOnCompleteListener((OnCompleteListener<DocumentSnapshot>) task -> {
                    if(task.getResult().exists()) {
                        inBookmarks = true;
                        menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_24);
                    }else{
                        inBookmarks = false;
                        menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_border_24);
                    }
        }).addOnFailureListener(e -> {
        });
    }
}