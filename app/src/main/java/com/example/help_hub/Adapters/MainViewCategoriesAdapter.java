package com.example.help_hub.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Fragments.NeedHelpFragment;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainViewCategoriesAdapter extends RecyclerView.Adapter {


    private List<Category> categoryList, modifiedCategory;
    private Context myContext;
    private NeedHelpFragment.NeedHelpAdapter needHelpAdapter;
    private List<NeedHelp> needHelpList, fullNeedHelpList;
    //private List<Category> category = new ArrayList<>();

    private FirebaseFirestore firebaseFirestore;

    //VIEW HOLDER STATIC CLASS
    public class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView singleCategoryTitle;
        public ImageView singleCategoryImageView;


        public AdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            singleCategoryTitle = itemView.findViewById(R.id.single_category_title_text_view);
            singleCategoryImageView = itemView.findViewById(R.id.single_category_image_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            modifiedCategory = new ArrayList<>(categoryList);
            modifiedCategory = modifiedCategory.get(getAdapterPosition()).subcategories;

            notifyDataSetChanged();
            categoryFilter(categoryList.get(getLayoutPosition()));
            needHelpAdapter.notifyDataSetChanged();
        }
    }

    //PUBLIC CONSTRUCTOR
    public MainViewCategoriesAdapter(Context myContext, List<Category> categoryList, NeedHelpFragment.NeedHelpAdapter adapter, List<NeedHelp> needHelpList, List<NeedHelp> fullNeedHelpList) {
        this.needHelpList = needHelpList;
        this.fullNeedHelpList = fullNeedHelpList;
        needHelpAdapter = adapter;
        modifiedCategory = categoryList;
        this.categoryList = categoryList;
        this.myContext = myContext;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_category_card_view, parent, false);

        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Category category = modifiedCategory.get(position);
        String title = category.getTitle();
        TextView singleCategoryTitle = ((AdapterViewHolder) holder).singleCategoryTitle;
        ImageView singleCategoryImageView = ((AdapterViewHolder) holder).singleCategoryImageView;
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
        return modifiedCategory.size();
    }

    private void categoryFilter(Category category) {
        for (NeedHelp needHelp : fullNeedHelpList) {
            if(!needHelp.getCategory().equals(category.getTitle())) {
                needHelpList.remove(needHelp);
            }
        }
    }
}
