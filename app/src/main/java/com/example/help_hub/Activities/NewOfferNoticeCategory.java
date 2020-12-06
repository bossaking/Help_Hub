package com.example.help_hub.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.help_hub.OtherClasses.Category;

import java.util.List;

public class NewOfferNoticeCategory extends AppCompatActivity {

    public interface OnTitleChangedListener{
        void onTitleChanged();
    }

    public OnTitleChangedListener onTitleChangedListener;

    public void SetOnTitleChangedListener(OnTitleChangedListener onTitleChangedListener){
        this.onTitleChangedListener = onTitleChangedListener;
    }

    public void titleChanged(){
        if(onTitleChangedListener != null){
            onTitleChangedListener.onTitleChanged();
        }
    }

    protected String categoryTitle = "";
    protected String subCategoryTitle = "";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void SelectCategory(){
        Intent intent = new Intent(getApplicationContext(), SelectCategoryActivity.class);
        startActivityForResult(intent ,1011);
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
}

