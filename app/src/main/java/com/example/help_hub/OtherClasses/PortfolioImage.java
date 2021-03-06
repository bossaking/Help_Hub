package com.example.help_hub.OtherClasses;

import android.net.Uri;

public class PortfolioImage {

    private String imageTitle;
    private Uri imageUri;
    private byte[] imageBytes;

    public PortfolioImage(String imageTitle, Uri uri) {
        this.imageTitle = imageTitle;
        this.imageUri = uri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}
