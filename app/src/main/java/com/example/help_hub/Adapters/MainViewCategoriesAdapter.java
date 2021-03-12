package com.example.help_hub.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.R;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainViewCategoriesAdapter extends RecyclerView.Adapter {


    private List<Category> categoryList;

    //VIEW HOLDER STATIC CLASS
    public static class AdapterViewHolder extends RecyclerView.ViewHolder{

        public TextView singleCategoryTitle;

        public AdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            singleCategoryTitle = itemView.findViewById(R.id.single_category_title_text_view);
        }
    }

    //PUBLIC CONSTRUCTOR
    public MainViewCategoriesAdapter(List<Category> categoryList){
        this.categoryList = categoryList;
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
        String title = categoryList.get(position).getTitle();
        TextView singleCategoryTitle = ((AdapterViewHolder)holder).singleCategoryTitle;
        singleCategoryTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
