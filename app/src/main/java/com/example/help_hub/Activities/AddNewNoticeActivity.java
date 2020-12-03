package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

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

public class AddNewNoticeActivity extends AppCompatActivity implements TextWatcher {

    private EditText mNewNoticeTitle;
    private Button addNewNoticeButton;

    private FirebaseFirestore firebaseFirestore;

    private Drawable defaultBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_notice);

        firebaseFirestore = FirebaseFirestore.getInstance();

        mNewNoticeTitle = findViewById(R.id.new_notice_title_edit_text);

        defaultBackground = mNewNoticeTitle.getBackground();
        mNewNoticeTitle.addTextChangedListener(this);

        addNewNoticeButton = findViewById(R.id.new_offer_add_offer_button);
        addNewNoticeButton.setOnClickListener(v -> {
            addNewNotice(mNewNoticeTitle.getText().toString().trim());
        });
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
}