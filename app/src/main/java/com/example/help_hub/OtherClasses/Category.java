package com.example.help_hub.OtherClasses;

import java.util.ArrayList;
import java.util.List;


public class Category {

    private String Id;
    public String parentCategoryId;
    private String Title;
    public List<Category> subcategories;

    public Category() {
        parentCategoryId = "";
        subcategories = new ArrayList<>();
    }

    public void setId(String id) {
        Id = id;
    }

    public String getId() {
        return Id;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle() {
        return Title;
    }
}
