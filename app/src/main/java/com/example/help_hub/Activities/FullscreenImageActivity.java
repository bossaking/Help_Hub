package com.example.help_hub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.help_hub.R;

public class FullscreenImageActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        imageView = findViewById(R.id.image_view);

        Glide.with(this).load(Uri.parse(getIntent().getExtras().getString("image"))).placeholder(R.drawable.image_with_progress)
                .error(R.drawable.broken_image_24).into(imageView);

    }
}