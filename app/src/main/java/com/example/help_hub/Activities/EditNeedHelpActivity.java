package com.example.help_hub.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help_hub.Adapters.PortfolioImagesRecyclerAdapter;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditNeedHelpActivity extends NewOfferNoticeCategory implements TextWatcher, PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    public static final String EXTRA_NEED_HELP_ID = "NEED_HELP_ID";
    public static final String EXTRA_NEED_HELP_TITLE = "NEED_HELP_TITLE";
    public static final String EXTRA_NEED_HELP_PRICE = "NEED_HELP_PRICE";
    public static final String EXTRA_NEED_HELP_DESCRIPTION = "NEED_HELP_DESCRIPTION";
    public static final String EXTRA_NEED_HELP_CATEGORY = "NEED_HELP_CATEGORY";
    public static final String EXTRA_NEED_HELP_SUBCATEGORY = "NEED_HELP_SUBCATEGORY";

    private EditText needHelpTitle;
    private EditText needHelpPrice;
    private EditText needHelpDescription;
    private Button editButton, categoriesButton;

    private FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    private Drawable defaultBackground;

    NeedHelp needHelp;

    private RecyclerView recyclerView;
    private PortfolioImagesRecyclerAdapter adapter;

    private List<PortfolioImage> needHelpImages;

    int imagesCount = 0;

    Context context;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_need_help);

        context = getApplicationContext();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();

        needHelp = new NeedHelp();

        needHelp.setId(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_ID));
        needHelp.setTitle(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_TITLE));
        needHelp.setPrice(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_PRICE));
        needHelp.setDescription(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_DESCRIPTION));
        needHelp.setCategory(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_CATEGORY));
        needHelp.setSubcategory(bundle.getString(EditNeedHelpActivity.EXTRA_NEED_HELP_SUBCATEGORY));

        needHelpTitle = findViewById(R.id.new_notice_title_edit_text);
        needHelpPrice = findViewById(R.id.new_notice_budget);
        needHelpDescription = findViewById(R.id.new_notice_description_edit_text);

        needHelpTitle.setText(needHelp.getTitle());
        needHelpPrice.setText(needHelp.getPrice());
        needHelpDescription.setText(needHelp.getDescription());

        defaultBackground = needHelpTitle.getBackground();
        needHelpTitle.addTextChangedListener(this);

        categoriesButton = findViewById(R.id.new_notice_select_category_button);
        categoryTitle = needHelp.getCategory();
        subCategoryTitle = needHelp.getSubcategory();
        categoriesButton.setText(needHelp.getCategory() + " / " + needHelp.getSubcategory());
        categoriesButton.setOnClickListener(v -> SelectCategory());
        SetOnTitleChangedListener(() -> {
            categoriesButton.setText(categoryTitle + " / " + subCategoryTitle);
        });

        editButton = findViewById(R.id.new_offer_add_offer_button);
        editButton.setText(getString(R.string.edit_button));
        editButton.setOnClickListener(v -> {
            title = needHelpTitle.getText().toString().trim();
            price = needHelpPrice.getText().toString().trim();
            description = needHelpDescription.getText().toString().trim();
            editNeedHelp();
        });

        getAllPhotos();

        adapter = new PortfolioImagesRecyclerAdapter(needHelpImages, this, this);

        recyclerView = findViewById(R.id.new_notice_images);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);
    }

    private void editNeedHelp() {
        if (!validateData() || !CheckForbiddenWords()) {
            return;
        }

        DocumentReference documentReference = firebaseFirestore.collection("announcement").document(needHelp.getId());

        Map<String, Object> editMap = new HashMap<>();
        editMap.put("Title", title);
        editMap.put("Price", price);
        editMap.put("Description", description);
        editMap.put("Category", categoryTitle);
        editMap.put("Subcategory", subCategoryTitle);

        documentReference.update(editMap).addOnCompleteListener(task -> {
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());


        for (int i = 0; i < imagesCount; i++) {
            StorageReference imageRef = storageReference.child("announcement/" + needHelp.getId() + "/images/photo" + i);
            imageRef.delete();
        }

        loadImagesToDatabase();
    }

    private void loadImagesToDatabase(){

        for (int i = 0; i < needHelpImages.size() - 1; i++) {
            PortfolioImage portfolioImage = needHelpImages.get(i);
            StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("announcement/" + needHelp.getId() + "/images/photo" + i);
            imgRef.putBytes(portfolioImage.getImageBytes()).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }

    }


    private boolean validateData() {
        if (title.isEmpty()) {
            needHelpTitle.setBackgroundResource(R.drawable.edit_error_border);
            needHelpTitle.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (price.isEmpty()) {
            needHelpPrice.setBackgroundResource(R.drawable.edit_error_border);
            needHelpPrice.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (categoryTitle.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_field_error), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        needHelpTitle.setBackground(defaultBackground);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {

                    Uri imageUri = clipData.getItemAt(i).getUri();

                    PortfolioImage needHelpImage = new PortfolioImage(
                            DocumentFile.fromSingleUri(getApplicationContext(), imageUri).getName(), imageUri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        needHelpImage.setImageBytes(imageBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }


                    AddImage(needHelpImage);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void getAllPhotos() {

        StorageReference imagesRef = storageReference.child("announcement/" + needHelp.getId() + "/images");
        needHelpImages = new ArrayList<>();
        AddImage(new PortfolioImage("DefaultImage", Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        imagesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String name = storageMetadata.getName();
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                            PortfolioImage image = new PortfolioImage(name, uri);
                            image.setImageBytes(bytes);
                            AddImage(image);
                            imagesCount++;
                            adapter.notifyDataSetChanged();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
                    }).addOnCompleteListener(task -> {
                    });
                });
            }
        });
    }

    public void AddImage(PortfolioImage portfolioImage) {
        if (getNeedHelpImagesCount() == 0) {
            needHelpImages.add(needHelpImages.size(), portfolioImage);
        } else {
            needHelpImages.add(needHelpImages.size() - 1, portfolioImage);
        }
    }

    @Override
    public void onImageClick(int position) {
        if (position == getNeedHelpImagesCount() - 1)
            addNewNeedHelpPhotos();
    }

    private void addNewNeedHelpPhotos() {
        Intent intent;

        try {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onImageLongClick(int position) {
        if (position == getNeedHelpImagesCount() - 1)
            return;

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c -> {
            needHelpImages.remove(getImage(position));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    public int getNeedHelpImagesCount() {
        return needHelpImages.size();
    }

    public PortfolioImage getImage(int position) {
        return needHelpImages.get(position);
    }
}
