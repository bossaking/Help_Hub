package com.example.help_hub.Singletones;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.help_hub.OtherClasses.PortfolioImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserPortfolioImagesDatabase {

    public interface ArrayChangedListener {
        void ArrayChanged();
    }

    private String userId;
    private List<PortfolioImage> portfolioImages;

    public ArrayChangedListener arrayChangedListener;

    private StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    public static UserPortfolioImagesDatabase instance = null;

    private Context context;

    private UserPortfolioImagesDatabase(Activity myActivity) {
        this.context = myActivity.getApplicationContext();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public static UserPortfolioImagesDatabase getInstance(Activity activity) {
        if (instance == null) instance = new UserPortfolioImagesDatabase(activity);
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public void Initialize(String userId) {
        this.userId = userId;
        GetAllPhotosFromFirebase();
    }

    public void LoadPortfolioImageToDatabase(PortfolioImage portfolioImage) {
        StorageReference imgRef = storageReference.child("users/" + userId + "/portfolio photos/" + portfolioImage.getImageTitle());
        imgRef.putFile(portfolioImage.getImageUri())
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    public void DeletePortfolioImageFromFirebase(PortfolioImage portfolioImage) {
        StorageReference imgRef = storageReference.child("users/" + userId + "/portfolio photos/" + portfolioImage.getImageTitle());
        portfolioImages.remove(portfolioImage);
        imgRef.delete();
    }

    public void GetAllPhotosFromFirebase() {
        StorageReference imagesRef = storageReference.child("users/" + userId + "/portfolio photos");
        portfolioImages = new ArrayList<>();
        AddNewImage(new PortfolioImage("DefaultImage", Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        imagesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getMetadata()
                        .addOnSuccessListener(storageMetadata -> {
                            String name = storageMetadata.getName();
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> AddNewImage(new PortfolioImage(name, uri)))
                                    .addOnCompleteListener(task -> {
                                    });
                        });
            }
        });
    }

    public List<PortfolioImage> GetPortfolioImages() {
        return portfolioImages;
    }

    public void AddNewImage(PortfolioImage portfolioImage) {
        if (GetPortfolioImagesCount() == 0)
            portfolioImages.add(portfolioImages.size(), portfolioImage);
        else portfolioImages.add(portfolioImages.size() - 1, portfolioImage);

        if (arrayChangedListener != null) arrayChangedListener.ArrayChanged();
    }

    public int GetPortfolioImagesCount() {
        return portfolioImages.size();
    }

    public PortfolioImage GetImage(int position) {
        return portfolioImages.get(position);
    }
}
