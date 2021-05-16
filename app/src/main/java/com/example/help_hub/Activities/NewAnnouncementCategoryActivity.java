package com.example.help_hub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewAnnouncementCategoryActivity extends AppCompatActivity {

    public interface OnTitleChangedListener {
        void onTitleChanged();
    }

    public interface OnForbiddenWordsLoadsListener {
        void onForbiddenWordsLoads();
    }

    public OnTitleChangedListener onTitleChangedListener;
    public OnForbiddenWordsLoadsListener onForbiddenWordsLoadsListener;

    public void SetOnTitleChangedListener(OnTitleChangedListener onTitleChangedListener) {
        this.onTitleChangedListener = onTitleChangedListener;
    }

    public void SetOnForbiddenWordsLoadsListener(OnForbiddenWordsLoadsListener onForbiddenWordsLoadsListener) {
        this.onForbiddenWordsLoadsListener = onForbiddenWordsLoadsListener;
    }

    public void titleChanged() {
        if (onTitleChangedListener != null) onTitleChangedListener.onTitleChanged();
    }

    public void forbiddenWordsLoads() {
        if (onForbiddenWordsLoadsListener != null)
            onForbiddenWordsLoadsListener.onForbiddenWordsLoads();
    }

    protected String title, price, description, categoryTitle = "", subCategoryTitle = "";
    private List<String> forbiddenWords;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForbiddenWords();

        onForbiddenWordsLoadsListener = () -> {
        };
    }

    protected void SelectCategory() {
        Intent intent = new Intent(getApplicationContext(), SelectCategoryActivity.class);
        startActivityForResult(intent, 1011);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1011 && resultCode == RESULT_OK) {
            categoryTitle = data.getStringExtra("CAT_TITLE");
            subCategoryTitle = data.getStringExtra("SUBCAT_TITLE");
            titleChanged();
        }
    }

    private void getForbiddenWords() {
        forbiddenWords = new ArrayList<>();

        CollectionReference collection = FirebaseFirestore.getInstance().collection("forbiddenWords");
        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    forbiddenWords.add(doc.getString("word"));
                }

                forbiddenWordsLoads();
            } else
                Toast.makeText(getApplicationContext(), getString(R.string.error) + task.getException(), Toast.LENGTH_LONG).show();
        });
    }

    protected boolean CheckForbiddenWords() {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        for (String word : forbiddenWords) {
            if (title.toLowerCase().contains(word) || description.toLowerCase().contains(word)) {
                loadingDialog.DismissDialog();
                Toast.makeText(this, R.string.forbidden_words_error, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        loadingDialog.DismissDialog();

        return true;
    }
}

