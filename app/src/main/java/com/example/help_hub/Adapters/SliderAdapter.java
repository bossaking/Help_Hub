package com.example.help_hub.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.help_hub.R;
import com.makeramen.roundedimageview.RoundedImageView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private List<Uri> imagesUri;
    private ViewPager2 pager2;
    private Context mContext;

    public SliderAdapter(List<Uri> imagesUri, ViewPager2 pager2, Context mContext){
        this.imagesUri = imagesUri;
        this.pager2 = pager2;
        this.mContext = mContext;
    }

    @NonNull
    @NotNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_image_container, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SliderViewHolder holder, int position) {
        holder.setImage(imagesUri.get(position));
    }

    @Override
    public int getItemCount() {
        return imagesUri != null ? imagesUri.size() : 0;
    }

    public class SliderViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView imageView;
        public SliderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slide);
        }

        public void setImage(Uri imageUri){
            Glide.with(mContext).load(imageUri).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(imageView);
        }
    }
}
