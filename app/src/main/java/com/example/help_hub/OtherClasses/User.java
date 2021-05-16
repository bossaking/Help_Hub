package com.example.help_hub.OtherClasses;

import android.net.Uri;

public class User {

    private String Name, PhoneNumber, City, Description, Id, Role;
    private float AllRating, UserRating, AllOpinionsCount;
    private Uri ProfileImage;

    public User(String Id) {
        this.Id = Id;
    }

    public User() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Uri getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(Uri profileImage) {
        ProfileImage = profileImage;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public void setAllRating(float allRating) {
        AllRating = allRating;
    }

    public float getAllRating() {
        return AllRating;
    }

    public void setUserRating(float userRating) {
        UserRating = userRating;
    }

    public float getUserRating() {
        return UserRating;
    }

    public void setAllOpinionsCount(float allOpinionsCount) {
        AllOpinionsCount = allOpinionsCount;
    }

    public float getAllOpinionsCount() {
        return AllOpinionsCount;
    }
}
