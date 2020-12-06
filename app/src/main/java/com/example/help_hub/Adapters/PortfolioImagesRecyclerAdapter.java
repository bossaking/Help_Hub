package com.example.help_hub.Adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.OtherClasses.PortfolioImage;
import com.example.help_hub.R;

import java.util.List;

public class PortfolioImagesRecyclerAdapter extends RecyclerView.Adapter {


    public interface OnClickListener{
        void onImageClick(int position);
    }
    public interface  OnLongClickListener{
        void onImageLongClick(int position);
    }


    private List<PortfolioImage> images;
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public static class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ImageView imageView;
        OnClickListener onClickListener;
        OnLongClickListener onLongClickListener;

        public AdapterViewHolder(@NonNull View itemView, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.portfolio_image);
            this.onClickListener = onClickListener;
            this.onLongClickListener = onLongClickListener;
            imageView.setOnClickListener(this);
            imageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onImageClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onLongClickListener.onImageLongClick(getAdapterPosition());
            return true;
        }
    }

    public PortfolioImagesRecyclerAdapter(List<PortfolioImage> images, OnClickListener onClickListener, OnLongClickListener onLongClickListener){
        this.images = images;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_image_card, parent, false);
        AdapterViewHolder adapterViewHolder = new AdapterViewHolder(view, onClickListener, onLongClickListener);
        return adapterViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Uri currentUri = images.get(position).getImageUri();
        ImageView imageView = ((AdapterViewHolder)holder).imageView;
        Glide.with(holder.itemView).load(currentUri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
