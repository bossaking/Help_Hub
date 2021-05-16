package com.example.help_hub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddWantToHelpActivity extends NewOfferNoticeCategory implements TextWatcher {

    private EditText wantToHelpTitle, wantToHelpDescription, wantToHelpPrice;
    private Button addButton, categoriesButton;
    private Drawable defaultBackground;

    private String userId, userCity;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private Context context;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_want_to_help);

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

        wantToHelpTitle = findViewById(R.id.new_offer_title_edit_text);
        wantToHelpPrice = findViewById(R.id.new_offer_cost);
        wantToHelpDescription = findViewById(R.id.new_offer_description_edit_text);

        defaultBackground = wantToHelpTitle.getBackground();
        wantToHelpTitle.addTextChangedListener(this);

        addButton = findViewById(R.id.new_offer_add_offer_button);
        addButton.setOnClickListener(v -> {
            title = wantToHelpTitle.getText().toString().trim();
            price = wantToHelpPrice.getText().toString().trim();
            description = wantToHelpDescription.getText().toString().trim();
            addNewWantToHelp();
        });

        categoriesButton = findViewById(R.id.new_offer_select_category_button);
        categoriesButton.setOnClickListener(v -> SelectCategory());
        SetOnTitleChangedListener(() -> categoriesButton.setText(categoryTitle + " / " + subCategoryTitle));
    }

    private void addNewWantToHelp() {
        if (!validateData() || !CheckForbiddenWords()) return;

        Map<String, Object> wantToHelpMap = new HashMap<>();
        wantToHelpMap.put("Title", title);
        wantToHelpMap.put("Price", price);
        wantToHelpMap.put("Description", description);
        wantToHelpMap.put("Category", categoryTitle);
        wantToHelpMap.put("Subcategory", subCategoryTitle);
        wantToHelpMap.put("UserId", userId);
        wantToHelpMap.put("ShowsCount", 0);
        wantToHelpMap.put("City", userCity);

        firebaseFirestore.collection("offers").document().set(wantToHelpMap).addOnSuccessListener(v -> {
            Toast.makeText(context, getString(R.string.success), Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(context, getString(R.string.error) + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateData() {
        if (title.isEmpty()) {
            wantToHelpTitle.setBackgroundResource(R.drawable.edit_error_border);
            wantToHelpTitle.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (price.isEmpty()) {
            wantToHelpPrice.setBackgroundResource(R.drawable.edit_error_border);
            wantToHelpPrice.setError(getString(R.string.empty_field_error));
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
        wantToHelpTitle.setBackground(defaultBackground);
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