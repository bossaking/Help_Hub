package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.R;
import com.example.help_hub.Singletones.UserPortfolioImagesDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewNoticeActivity extends AppCompatActivity implements TextWatcher, PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    private EditText mNewNoticeTitle;
    private Button addNewNoticeButton;

    private FirebaseFirestore firebaseFirestore;

    private Drawable defaultBackground;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;


    private List<PortfolioImage> noticeImages;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_notice);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();

        mNewNoticeTitle = findViewById(R.id.new_notice_title_edit_text);

        defaultBackground = mNewNoticeTitle.getBackground();
        mNewNoticeTitle.addTextChangedListener(this);

        addNewNoticeButton = findViewById(R.id.new_offer_add_offer_button);
        addNewNoticeButton.setOnClickListener(v -> {
            addNewNotice(mNewNoticeTitle.getText().toString().trim());
        });

        context = getApplicationContext();

        noticeImages = new ArrayList<>();
        noticeImages.add(new PortfolioImage("DefaultImage", Uri.parse("android.resource://" + context.getPackageName() + "/drawable/add_a_photo_24")));

        adapter = new PortfolioImagesRecyclerAdapter(noticeImages, this, this);

        recyclerView = findViewById(R.id.new_notice_images);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);
    }

    private void addNewNotice(String noticeTitle) {
        if (!validateTitle(noticeTitle)) {
            return;
        }
        Map<String, Object> noticeMap = new HashMap<>();
        noticeMap.put("Title", noticeTitle);

        firebaseFirestore.collection("notices").document().set(noticeMap).addOnSuccessListener(v -> {
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(getApplicationContext(), "Error: " + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateTitle(String offerTitle) {
        if (offerTitle.isEmpty()) {
            mNewNoticeTitle.setBackgroundResource(R.drawable.edit_error_border);
            mNewNoticeTitle.setError(getString(R.string.empty_field_error));
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

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            ClipData clipData = data.getClipData();
            if(clipData != null){
                for(int i = 0; i < clipData.getItemCount(); i++){

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

        try{
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }catch (Exception e){
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onImageClick(int position) {
        if(position == getNoticeImagesCount() - 1)
            addNewNoticePhotos();
    }

    @Override
    public void onImageLongClick(int position) {
        if(position == getNoticeImagesCount() - 1)
            return;

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c->{
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

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}