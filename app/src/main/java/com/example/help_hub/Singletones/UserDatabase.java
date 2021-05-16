package com.example.help_hub.Singletones;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.help_hub.OtherClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserDatabase {

    public interface ProfileDataLoaded {
        void ProfileDataLoaded();
    }

    public interface ProfileImageLoaded {
        void ProfileImageLoaded(Uri uri);
    }

    public interface ProfileImageChanged {
        void ProfileImageChanged(Uri uri);
    }

    private String userId;
    private User user;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    private Context context;

    public static UserDatabase instance;

    public ProfileDataLoaded profileDataLoaded;
    public ProfileImageLoaded profileImageLoaded;
    public ProfileImageChanged profileImageChanged;

    private UserDatabase(Activity myActivity, String userId) {
        this.context = myActivity.getApplicationContext();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        this.userId = userId;
        getUserFromFirebase(userId);
    }

    public static UserDatabase getInstance(Activity activity, String userId) {
        if (instance == null) instance = new UserDatabase(activity, userId);

        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

   /* public User getOtherUserInformations(String userId) {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);
        User user = new User(userId);
        documentReference.addSnapshotListener((documentSnapshot, e) -> {
            user.setName(documentSnapshot.getString("Name"));
            user.setPhoneNumber(documentSnapshot.getString("Phone number"));
            user.setCity(documentSnapshot.getString("City"));
            user.setDescription(documentSnapshot.getString("Description"));
            user.setRole(documentSnapshot.getString("Role"));
            if (profileDataLoaded != null) {
                profileDataLoaded.ProfileDataLoaded();
            }

        });

        //PROFILE IMAGE
        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            user.setProfileImage(uri);
            if (profileImageLoaded != null) {
                profileImageLoaded.ProfileImageLoaded(uri);
            }
        }).addOnFailureListener(e -> {
            user.setProfileImage(Uri.parse("android.resource://" + context.getPackageName() + "/drawable/default_user_image"));
            if (profileImageLoaded != null) {
                profileImageLoaded.ProfileImageLoaded(user.getProfileImage());
            }
            Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });

        return user;
    }*/

    public void getUserFromFirebase(String userId) {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);
        user = new User(userId);

        documentReference.addSnapshotListener((documentSnapshot, e) -> {
            user.setName(documentSnapshot.getString("Name"));
            user.setPhoneNumber(documentSnapshot.getString("Phone number"));
            user.setCity(documentSnapshot.getString("City"));
            user.setDescription(documentSnapshot.getString("Description"));
            user.setRole(documentSnapshot.getString("Role"));

            if (documentSnapshot.contains("UserRating")) {
                user.setUserRating(documentSnapshot.getLong("UserRating"));
                user.setAllOpinionsCount(documentSnapshot.getLong("AllOpinionsCount"));
            } else user.setUserRating(0);

            if (profileDataLoaded != null) profileDataLoaded.ProfileDataLoaded();
        });

        //PROFILE IMAGE
        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setProfileImage(uri);
                    if (profileImageLoaded != null) profileImageLoaded.ProfileImageLoaded(uri);
                })
                .addOnFailureListener(e -> {
                    user.setProfileImage(Uri.parse("android.resource://" + context.getPackageName() + "/drawable/default_user_image"));
                    if (profileImageLoaded != null)
                        profileImageLoaded.ProfileImageLoaded(user.getProfileImage());
                    //Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void setUserProfileImage(Uri imageUri) {
        user.setProfileImage(imageUri);
        loadUserProfileImageToFirebase();
    }

    private void loadUserProfileImageToFirebase() {
        StorageReference fileRef = storageReference.child("users/" + userId + "/profile.jpg");
        fileRef.putFile(user.getProfileImage())
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(context, "Photo has been loaded", Toast.LENGTH_SHORT).show();
                    if (profileImageChanged != null)
                        profileImageChanged.ProfileImageChanged(user.getProfileImage());
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    public User getUser() {
        return user;
    }
}
