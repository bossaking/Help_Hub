package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.Adapters.PortfolioImagesRecyclerAdapter;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNeedHelpActivity extends NewAnnouncementCategoryActivity implements TextWatcher, PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    private EditText needHelpTitle, needHelpDescription, needHelpPrice;
    private Button addButton, categoriesButton;
    private Drawable defaultBackground;

    private String userId, userCity;
    private List<PortfolioImage> needHelpImages;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private Context context;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_need_help);

        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getApplicationContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getUid();

        firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userCity = task.getResult().getString("City");
                loadingDialog.DismissDialog();
            }
        });

        needHelpTitle = findViewById(R.id.new_notice_title_edit_text);
        needHelpPrice = findViewById(R.id.new_notice_budget);
        needHelpDescription = findViewById(R.id.new_notice_description_edit_text);

        defaultBackground = needHelpTitle.getBackground();
        needHelpTitle.addTextChangedListener(this);

        addButton = findViewById(R.id.new_offer_add_offer_button);
        addButton.setOnClickListener(v -> {
            title = needHelpTitle.getText().toString().trim();
            price = needHelpPrice.getText().toString().trim();
            description = needHelpDescription.getText().toString().trim();
            addNewNeedHelp();
        });

        categoriesButton = findViewById(R.id.new_notice_select_category_button);
        categoriesButton.setOnClickListener(v -> SelectCategory());
        SetOnTitleChangedListener(() -> categoriesButton.setText(categoryTitle + " / " + subCategoryTitle));

        needHelpImages = new ArrayList<>();
        needHelpImages.add(new PortfolioImage("DefaultImage", Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        adapter = new PortfolioImagesRecyclerAdapter(needHelpImages, this, this);

        recyclerView = findViewById(R.id.new_notice_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void addNewNeedHelp() {
        if (!validateData() || !CheckForbiddenWords()) return;

        Map<String, Object> needHelpMap = new HashMap<>();
        needHelpMap.put("Title", title);
        needHelpMap.put("Price", price);
        needHelpMap.put("Description", description);
        needHelpMap.put("Category", categoryTitle);
        needHelpMap.put("Subcategory", subCategoryTitle);
        needHelpMap.put("UserId", userId);
        needHelpMap.put("ShowsCount", 0);
        needHelpMap.put("City", userCity);

        String id = firebaseFirestore.collection("announcement").document().getId();
        firebaseFirestore.collection("announcement").document(id).set(needHelpMap).addOnSuccessListener(v -> {
            Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(getApplicationContext(), getString(R.string.error) + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());

        for (int i = 0; i < needHelpImages.size() - 1; i++) {
            PortfolioImage portfolioImage = needHelpImages.get(i);
            StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("announcement/" + id + "/images/photo" + i);
            imgRef.putFile(portfolioImage.getImageUri())
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(context, getString(R.string.success), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, getString(R.string.error) + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    PortfolioImage noticeImage = new PortfolioImage(DocumentFile.fromSingleUri(context,
                            clipData.getItemAt(i).getUri()).getName(), clipData.getItemAt(i).getUri());

                    needHelpImages.add(0, noticeImage);
                }
            }
        }

        adapter.notifyDataSetChanged();
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
    public void onImageClick(int position) {
        if (position == getNeedHelpImagesCount() - 1) addNewNeedHelpPhotos();
    }

    @Override
    public void onImageLongClick(int position) {
        if (position == getNeedHelpImagesCount() - 1) return;

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}