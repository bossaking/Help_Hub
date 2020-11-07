package com.example.help_hub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class UserPortfolioPhotosActivity extends AppCompatActivity implements PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_portfolio_photos);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        database = Database.getInstance(this);
        adapter = new PortfolioImagesRecyclerAdapter(database.GetPortfolioImages(), this, this);
        database.arrayChangedListener = () -> {
            adapter.notifyDataSetChanged();
        };
        recyclerView = findViewById(R.id.portfolio_images_recycler_view);
        recyclerView.setHasFixedSize(true);



        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            ClipData clipData = data.getClipData();
            if(clipData != null){
                for(int i = 0; i < clipData.getItemCount(); i++){

                    PortfolioImage portfolioImage = new PortfolioImage(DocumentFile.fromSingleUri(getApplicationContext(),
                            clipData.getItemAt(i).getUri()).getName(), clipData.getItemAt(i).getUri());

                    database.AddNewImage(portfolioImage);
                    database.LoadPortfolioImageToDatabase(portfolioImage);
                }
                adapter.notifyDataSetChanged();
            }
        }

    }

    private void AddNewPortfolioPhotos(){

        Intent intent;

        try{
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }catch (Exception e){
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 100);

    }

    @Override
    public void onImageClick(int position) {
        if(position == database.GetPortfolioImagesCount() - 1)
            AddNewPortfolioPhotos();
    }

    @Override
    public void onImageLongClick(int position) {
        if(position == database.GetPortfolioImagesCount() - 1)
            return;

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c->{
            database.DeletePortfolioImageFromFirebase(database.GetImage(position));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}