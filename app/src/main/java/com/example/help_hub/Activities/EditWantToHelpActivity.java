package com.example.help_hub.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.help_hub.OtherClasses.WantToHelp;
import com.example.help_hub.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditWantToHelpActivity extends NewAnnouncementCategoryActivity implements TextWatcher {

    public static final String EXTRA_WANT_TO_HELP_ID = "WANT_TO_HELP_ID",
            EXTRA_WANT_TO_HELP_TITLE = "WANT_TO_HELP_TITLE",
            EXTRA_WANT_TO_HELP_PRICE = "WANT_TO_HELP_PRICE",
            EXTRA_WANT_TO_HELP_DESCRIPTION = "WANT_TO_HELP_DESCRIPTION",
            EXTRA_WANT_TO_HELP_CATEGORY = "WANT_TO_HELP_CATEGORY",
            EXTRA_WANT_TO_HELP_SUBCATEGORY = "WANT_TO_HELP_SUBCATEGORY";

    private EditText wantToHelpTitle, wantToHelpPrice, wantToHelpDescription;
    private Button editButton, categoriesButton;
    private Drawable defaultBackground;

    private WantToHelp wantToHelp;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_want_to_help);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();

        Bundle bundle = getIntent().getExtras();

        wantToHelp = new WantToHelp();

        wantToHelp.setId(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_ID));
        wantToHelp.setTitle(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_TITLE));
        wantToHelp.setPrice(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_PRICE));
        wantToHelp.setDescription(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_DESCRIPTION));
        wantToHelp.setCategory(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_CATEGORY));
        wantToHelp.setSubcategory(bundle.getString(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_SUBCATEGORY));

        wantToHelpTitle = findViewById(R.id.new_offer_title_edit_text);
        wantToHelpPrice = findViewById(R.id.new_offer_cost);
        wantToHelpDescription = findViewById(R.id.new_offer_description_edit_text);

        wantToHelpTitle.setText(wantToHelp.getTitle());
        wantToHelpPrice.setText(wantToHelp.getPrice());
        wantToHelpDescription.setText(wantToHelp.getDescription());

        defaultBackground = wantToHelpTitle.getBackground();
        wantToHelpTitle.addTextChangedListener(this);

        categoriesButton = findViewById(R.id.new_offer_select_category_button);
        categoryTitle = wantToHelp.getCategory();
        subCategoryTitle = wantToHelp.getSubcategory();
        categoriesButton.setText(wantToHelp.getCategory() + " / " + wantToHelp.getSubcategory());
        categoriesButton.setOnClickListener(v -> SelectCategory());

        SetOnTitleChangedListener(() -> categoriesButton.setText(categoryTitle + " / " + subCategoryTitle));

        editButton = findViewById(R.id.new_offer_add_offer_button);
        editButton.setText(getString(R.string.edit_button));
        editButton.setOnClickListener(v -> {
            title = wantToHelpTitle.getText().toString().trim();
            price = wantToHelpPrice.getText().toString().trim();
            description = wantToHelpDescription.getText().toString().trim();
            editWantToHelp();
        });
    }

    private void editWantToHelp() {
        if (!validateData() || !CheckForbiddenWords()) return;

        DocumentReference documentReference = firebaseFirestore.collection("offers").document(wantToHelp.getId());

        Map<String, Object> editWantToHelpMap = new HashMap<>();
        editWantToHelpMap.put("Title", title);
        editWantToHelpMap.put("Price", price);
        editWantToHelpMap.put("Description", description);
        editWantToHelpMap.put("Category", categoryTitle);
        editWantToHelpMap.put("Subcategory", subCategoryTitle);

        documentReference.update(editWantToHelpMap).addOnCompleteListener(task -> {
            Toast.makeText(getApplicationContext(), R.string.Updated, Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), R.string.error + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
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
