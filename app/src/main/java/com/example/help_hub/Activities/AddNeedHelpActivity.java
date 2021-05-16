package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNeedHelpActivity extends NewOfferNoticeCategory implements TextWatcher, PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    private EditText mNewNoticeTitle;
    private EditText mNewNoticeDescription;
    private EditText mNewNoticePrice;
    private Button addNewNoticeButton, categoriesButton;

    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    private Drawable defaultBackground;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    String userId;
    String userCity;

    private List<PortfolioImage> noticeImages;

    Context context;

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

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getUid();
        firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               userCity = task.getResult().getString("City");
               loadingDialog.DismissDialog();
           }
        });

        mNewNoticeTitle = findViewById(R.id.new_notice_title_edit_text);
        mNewNoticePrice = findViewById(R.id.new_notice_budget);
        mNewNoticeDescription = findViewById(R.id.new_notice_description_edit_text);
        defaultBackground = mNewNoticeTitle.getBackground();
        mNewNoticeTitle.addTextChangedListener(this);

        addNewNoticeButton = findViewById(R.id.new_offer_add_offer_button);
        addNewNoticeButton.setOnClickListener(v -> {
            title = mNewNoticeTitle.getText().toString().trim();
            price = mNewNoticePrice.getText().toString().trim();
            description = mNewNoticeDescription.getText().toString().trim();
            addNewNotice();
        });

        categoriesButton = findViewById(R.id.new_notice_select_category_button);
        categoriesButton.setOnClickListener(v -> SelectCategory());
        SetOnTitleChangedListener(() -> {
            categoriesButton.setText(categoryTitle + " / " + subCategoryTitle);
        });

        context = getApplicationContext();

        noticeImages = new ArrayList<>();
        noticeImages.add(new PortfolioImage("DefaultImage", Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        adapter = new PortfolioImagesRecyclerAdapter(noticeImages, this, this);

        recyclerView = findViewById(R.id.new_notice_images);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);
    }

    private void addNewNotice() {
        if (!validateData() || !CheckForbiddenWords()) {
            return;
        }
        Map<String, Object> noticeMap = new HashMap<>();
        noticeMap.put("Title", title);
        noticeMap.put("Price", price);
        noticeMap.put("Description", description);
        noticeMap.put("Category", categoryTitle);
        noticeMap.put("Subcategory", subCategoryTitle);
        noticeMap.put("UserId", userId);
        noticeMap.put("ShowsCount", 0);
        noticeMap.put("City", userCity);

        String id = firebaseFirestore.collection("announcement").document().getId();
        firebaseFirestore.collection("announcement").document(id).set(noticeMap).addOnSuccessListener(v -> {
            Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(getApplicationContext(), getString(R.string.error) + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());

        for (int i = 0; i < noticeImages.size() - 1; i++) {
            PortfolioImage portfolioImage = noticeImages.get(i);
            StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("announcement/" + id + "/images/photo" + i);
            imgRef.putFile(portfolioImage.getImageUri()).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(context, getString(R.string.success), Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, getString(R.string.error) + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private boolean validateData() {
        if (title.isEmpty()) {
            mNewNoticeTitle.setBackgroundResource(R.drawable.edit_error_border);
            mNewNoticeTitle.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (price.isEmpty()) {
            mNewNoticePrice.setBackgroundResource(R.drawable.edit_error_border);
            mNewNoticePrice.setError(getString(R.string.empty_field_error));
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
        mNewNoticeTitle.setBackground(defaultBackground);
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

                    PortfolioImage noticeImage = new PortfolioImage(DocumentFile.fromSingleUri(getApplicationContext(),
                            clipData.getItemAt(i).getUri()).getName(), clipData.getItemAt(i).getUri());

                    noticeImages.add(0, noticeImage);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void addNewNoticePhotos() {
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
        if (position == getNoticeImagesCount() - 1)
            addNewNoticePhotos();
    }

    @Override
    public void onImageLongClick(int position) {
        if (position == getNoticeImagesCount() - 1)
            return;

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c -> {
            noticeImages.remove(getImage(position));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    public int getNoticeImagesCount() {
        return noticeImages.size();
    }

    public PortfolioImage getImage(int position) {
        return noticeImages.get(position);
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