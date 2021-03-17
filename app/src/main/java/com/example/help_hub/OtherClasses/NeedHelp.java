package com.example.help_hub.OtherClasses;

import android.net.Uri;

public class NeedHelp {

    private String Title, Price, Description, Id, UserId, Category, Subcategory, City;
    private Integer ShowsCount;

    public NeedHelp() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public Integer getShowsCount() {
        return ShowsCount;
    }

    public void setShowsCount(Integer showsCount) {
        ShowsCount = showsCount;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getSubcategory() {
        return Subcategory;
    }

    public void setSubcategory(String subcategory) {
        Subcategory = subcategory;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getCity() {
        return City;
    }
}
