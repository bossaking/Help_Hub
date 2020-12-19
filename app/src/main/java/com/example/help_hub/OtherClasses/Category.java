package com.example.help_hub.OtherClasses;

import java.util.ArrayList;
import java.util.List;


public class Category {

    public String id;
    public String parentCategoryId;
    public String title;
    public List<Category> subcategories;

    public Category() {
        parentCategoryId = "";
        subcategories = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
