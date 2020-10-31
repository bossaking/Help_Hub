package com.example.help_hub;

import android.net.Uri;

public class User {

    private String Name, PhoneNumber, City, Description, Id;
    private Uri ProfileImage;



    public User(String Id){
        this.Id = Id;
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
}
