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

import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNeedHelpActivity extends NewOfferNoticeCategory implements TextWatcher {

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

    private Drawable defaultBackground;

    NeedHelp needHelp;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_need_help);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();

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
        needHelpPrice.setText(needHelp.getPrice() + " " + getString(R.string.new_notice_currency));
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
            price = price.substring(0, price.length() - 2).trim();
            description = needHelpDescription.getText().toString().trim();
            editNeedHelp();
        });
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
}
