package com.example.help_hub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class User_Portfolio_Photos_Fragment extends Fragment implements PortfolioImagesRecyclerAdapter.OnClickListener, PortfolioImagesRecyclerAdapter.OnLongClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    UserActivity userActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_portfolio_photos_fragment_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userActivity = (UserActivity) getActivity();

        recyclerView = getActivity().findViewById(R.id.portfolio_images_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new PortfolioImagesRecyclerAdapter(userActivity.userPortfolioPhotos, this, this);

        recyclerView.setAdapter(adapter);



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        adapter.notifyDataSetChanged();
    }

    private void AddNewPortfolioPhotos(){
        Intent intent;

        try{
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }catch (Exception e){
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        getActivity().startActivityForResult(intent, 100);
    }

    @Override
    public void onImageClick(int position) {
        if(position == userActivity.userPortfolioPhotos.size() - 1)
        AddNewPortfolioPhotos();
    }

    @Override
    public void onImageLongClick(int position) {
        if(position == userActivity.userPortfolioPhotos.size() - 1)
            return;

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.portfolio_photo_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Button deletePhoto = view.findViewById(R.id.delete_portfolio_photo);
        deletePhoto.setOnClickListener(c->{
            userActivity.userPortfolioPhotos.remove(position);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.setCancelable(true);
        dialog.show();
    }
}
