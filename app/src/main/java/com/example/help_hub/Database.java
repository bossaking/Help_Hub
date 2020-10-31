package com.example.help_hub;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.AsyncListDiffer;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class Database {

    public interface ArrayChangedListener {
        void ArrayChanged();
    }


    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    String userId;

    Context context;

    List<PortfolioImage> portfolioImages;


    public static Database instance = null;

    public ArrayChangedListener arrayChangedListener;

    private Database(Activity myActivity) {

        this.context = myActivity.getApplicationContext();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getUid();
    }

    public static Database getInstance(Activity activity) {

        if (instance == null) {
            instance = new Database(activity);
        }
        return instance;
    }

    public static void ClearInstance(){
        instance = null;
    }

    public void Initialize(){
        GetAllPhotosFromFirebase();
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

    public void GetAllPhotosFromFirebase() {

        StorageReference imagesRef = storageReference.child("users/" + userId + "/portfolio photos");
        portfolioImages = new ArrayList<>();
        AddNewImage(new PortfolioImage("DefaultImage",
                Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

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


    }

    public List<PortfolioImage> GetPortfolioImages() {
        return portfolioImages;
    }

    public void AddNewImage(PortfolioImage portfolioImage) {
        if(GetPortfolioImagesCount() == 0){
            portfolioImages.add(portfolioImages.size(), portfolioImage);
        }else{
            portfolioImages.add(portfolioImages.size() - 1, portfolioImage);
        }
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


}
