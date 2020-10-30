package com.example.help_hub;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.AsyncListDiffer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class Database {

    StorageReference storageReference;
    FirebaseAuth firebaseAuth;

    String userId;

    Context context;

    List<PortfolioImage> portfolioImages;

    private static Database instance;

    public ArrayChangedListener arrayChangedListener;

    private Database(Activity myActivity) {

        this.context = myActivity.getApplicationContext();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();
        this.portfolioImages = GetAllPhotosFromFirebase();
    }

    public static Database getInstance(Activity activity) {

        if (instance == null) {
            instance = new Database(activity);
        }
        return instance;
    }

    public void LoadPortfolioImageToDatabase(PortfolioImage portfolioImage) {
        StorageReference imgRef = storageReference.child("users/" + userId + "/portfolio photos/" + portfolioImage.getImageTitle());
        imgRef.putFile(portfolioImage.getImageUri()).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });
    }

    public void DeletePortfolioImageFromFirebase(PortfolioImage portfolioImage) {
        StorageReference imgRef = storageReference.child("users/" + userId + "/portfolio photos/" + portfolioImage.getImageTitle());
        portfolioImages.remove(portfolioImage);
        imgRef.delete();
    }

    public List<PortfolioImage> GetAllPhotosFromFirebase() {

        List<PortfolioImage> portfolioImages = new ArrayList<>();
        portfolioImages.add(new PortfolioImage("DefaultImage",
                Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        StorageReference imagesRef = storageReference.child("users/" + userId + "/portfolio photos");

        imagesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getMetadata().addOnSuccessListener((OnSuccessListener<StorageMetadata>) storageMetadata -> {
                    String name = storageMetadata.getName();
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        AddNewImage(new PortfolioImage(name, uri));
                    }).addOnCompleteListener(task -> {
                    });
                });
            }
        });
        return portfolioImages;

    }

    public List<PortfolioImage> GetPortfolioImages() {
        return portfolioImages;
    }

    public void AddNewImage(PortfolioImage portfolioImage) {
        portfolioImages.add(portfolioImages.size() - 1, portfolioImage);
        if (arrayChangedListener != null) {
            arrayChangedListener.ArrayChanged();
        }
    }

    public int GetPortfolioImagesCount() {
        return portfolioImages.size();
    }

    public PortfolioImage GetImage(int position) {
        return portfolioImages.get(position);
    }

    public interface ArrayChangedListener {
        void ArrayChanged();
    }

}
