package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.help_hub.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTheOfferActivity extends AppCompatActivity implements TextWatcher {

    private EditText mNewOfferTitle;
    private Button addNewOfferButton;

    private FirebaseFirestore firebaseFirestore;

    private Drawable defaultBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_the_offer);

        firebaseFirestore = FirebaseFirestore.getInstance();

        mNewOfferTitle = findViewById(R.id.new_offer_title_edit_text);

        defaultBackground = mNewOfferTitle.getBackground();
        mNewOfferTitle.addTextChangedListener(this);

        addNewOfferButton = findViewById(R.id.new_offer_add_offer_button);
        addNewOfferButton.setOnClickListener(v -> {
            addNewOffer(mNewOfferTitle.getText().toString().trim());
        });
    }

    private void addNewOffer(String offerTitle) {
        if (!validateTitle(offerTitle)) {
            return;
        }
        Map<String, Object> offerMap = new HashMap<>();
        offerMap.put("Title", offerTitle);

        firebaseFirestore.collection("offers").document().set(offerMap).addOnSuccessListener(v -> {
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(v -> Toast.makeText(getApplicationContext(), "Error: " + v.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateTitle(String offerTitle) {
        if (offerTitle.isEmpty()) {
            mNewOfferTitle.setBackgroundResource(R.drawable.edit_error_border);
            mNewOfferTitle.setError(getString(R.string.empty_field_error));
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
}