package com.example.help_hub.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainViewCategoriesAdapter extends RecyclerView.Adapter {


    private List<Category> categoryList;
    private Context myContext;

    //VIEW HOLDER STATIC CLASS
    public static class AdapterViewHolder extends RecyclerView.ViewHolder{

        public TextView singleCategoryTitle;
        public ImageView singleCategoryImageView;

        public AdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            singleCategoryTitle = itemView.findViewById(R.id.single_category_title_text_view);
            singleCategoryImageView = itemView.findViewById(R.id.single_category_image_view);
        }
    }

    //PUBLIC CONSTRUCTOR
    public MainViewCategoriesAdapter(Context myContext, List<Category> categoryList){
        this.categoryList = categoryList;
        this.myContext = myContext;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_category_card_view, parent, false);

        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        String title = category.getTitle();
        TextView singleCategoryTitle = ((AdapterViewHolder)holder).singleCategoryTitle;
        ImageView singleCategoryImageView = ((AdapterViewHolder)holder).singleCategoryImageView;
        singleCategoryTitle.setText(title);
        getImage(category, singleCategoryImageView);
    }

    private void getImage(Category category, ImageView image) {
        StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("category/" + category.getId() + "/image0.png");
        imgRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(image);
        }).addOnFailureListener(v -> {
            image.setImageResource(R.drawable.ic_baseline_missing_image_24);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
