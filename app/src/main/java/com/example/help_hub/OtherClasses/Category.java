package com.example.help_hub.OtherClasses;

import java.util.ArrayList;
import java.util.List;


public class Category {

    public String id;
    public String parentCategoryId;
    private String Title;
    public List<Category> subcategories;

    public Category() {
        parentCategoryId = "";
        subcategories = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle() {
        return Title;
    }
}
