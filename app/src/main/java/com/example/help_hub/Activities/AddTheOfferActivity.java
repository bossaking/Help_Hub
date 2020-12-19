package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTheOfferActivity extends NewOfferNoticeCategory implements TextWatcher {

    private EditText mNewOfferTitle;
    private EditText mNewOfferDescription;
    private EditText mNewNoticePrice;
    private Button addNewOfferButton, categoriesButton;

    private FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String userId;

    private Drawable defaultBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_want_to_help);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getUid();

        mNewOfferTitle = findViewById(R.id.new_offer_title_edit_text);
        mNewNoticePrice = findViewById(R.id.new_offer_cost);
        mNewOfferDescription = findViewById(R.id.new_offer_description_edit_text);

        defaultBackground = mNewOfferTitle.getBackground();
        mNewOfferTitle.addTextChangedListener(this);

        categoriesButton = findViewById(R.id.new_offer_select_category_button);
        categoriesButton.setOnClickListener(v -> SelectCategory());
        SetOnTitleChangedListener(() -> {
            categoriesButton.setText(categoryTitle + " / " + subCategoryTitle);
        });

        addNewOfferButton = findViewById(R.id.new_offer_add_offer_button);
        addNewOfferButton.setOnClickListener(v -> {
            title = mNewOfferTitle.getText().toString().trim();
            price = mNewNoticePrice.getText().toString().trim();
            description = mNewOfferDescription.getText().toString().trim();
            addNewOffer();
        });
    }

    private void addNewOffer() {
        if (!validateData() || !CheckForbiddenWords()) {
            return;
        }
        Map<String, Object> offerMap = new HashMap<>();
        offerMap.put("Title", title);
        offerMap.put("Price", price);
        offerMap.put("Description", description);
        offerMap.put("Category", categoryTitle);
        offerMap.put("Subcategory", subCategoryTitle);
        offerMap.put("UserId", userId);

        firebaseFirestore.collection("offers").document().set(offerMap).addOnSuccessListener(v -> {
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(getApplicationContext(), "Error: " + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateData() {
        if (title.isEmpty()) {
            mNewOfferTitle.setBackgroundResource(R.drawable.edit_error_border);
            mNewOfferTitle.setError(getString(R.string.empty_field_error));
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
        mNewOfferTitle.setBackground(defaultBackground);
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
}