package com.example.help_hub.Activities;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help_hub.Adapters.CategoriesAdapter;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends AppCompatActivity implements CategoriesAdapter.OnClickListener {

    protected List<Category> categories;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter categoriesAdapter;

    private int state = 0;

    String categoryTitle, subcategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        categories = new ArrayList<>();
        recyclerView = findViewById(R.id.categories_recycler_view);
        categoriesAdapter = new CategoriesAdapter(categories, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoriesAdapter);

        initializeItems();
    }

    private void initializeItems() {
        categories.clear();
        CollectionReference collection = FirebaseFirestore.getInstance().collection("categories");
        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Category newCategory = new Category();
                    newCategory.id = doc.getId();
                    newCategory.title = doc.getString("Title");
                    categories.add(newCategory);
                }
                categoriesAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getSubcategories(String parentId) {
        state++;
        CollectionReference collection = FirebaseFirestore.getInstance().collection("categories").document(parentId)
                .collection("Subcategories");
        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Category category = new Category();
                    category.parentCategoryId = parentId;
                    category.title = doc.getString("Title");
                    categories.add(category);
                }

                categoriesAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(), "Error: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCategoryClick(int position) {
        Category category = categories.get(position);
        if (category.parentCategoryId.isEmpty()) {
            categoryTitle = category.title;
            categories.clear();
            getSubcategories(category.getId());
            categoriesAdapter.notifyDataSetChanged();
        } else {
            subcategoryTitle = category.title;
            Intent data = new Intent();
            data.putExtra("CAT_TITLE", categoryTitle);
            data.putExtra("SUBCAT_TITLE", subcategoryTitle);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (state == 0) {
            finish();
        } else if (state == 1) {
            state--;
            initializeItems();
        }
    }
}