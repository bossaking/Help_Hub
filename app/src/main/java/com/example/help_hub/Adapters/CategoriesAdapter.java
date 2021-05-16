package com.example.help_hub.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.R;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter {

    private static final int TYPE_SUBCATEGORY = 0, TYPE_CATEGORY = 1;

    private List<Category> categories;

    private CategoriesAdapter.OnClickListener onClickListener;
    //private CategoriesAdapter.OnLongClickListener onLongClickListener;

    public interface OnClickListener {
        void onCategoryClick(int position);
    }

    public interface OnLongClickListener {
        void onCategoryLongClick(int position);
    }

    public static class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        CategoriesAdapter.OnClickListener onClickListener;
        //PortfolioImagesRecyclerAdapter.OnLongClickListener onLongClickListener;

        public AdapterViewHolder(@NonNull View itemView, OnClickListener onClickListener) {
            super(itemView);
            //imageView = itemView.findViewById(R.id.portfolio_image);
            textView = itemView.findViewById(R.id.category_title);
            this.onClickListener = onClickListener;
            textView.setOnClickListener(this);
            //this.onLongClickListener = onLongClickListener;
            //imageView.setOnClickListener(this);
            //imageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onCategoryClick(getAdapterPosition());
        }
    }

    public CategoriesAdapter(List<Category> categories, OnClickListener onClickListener) {
        this.categories = categories;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_CATEGORY)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_with_subcategories_card, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_without_subcategories_card, parent, false);

        CategoriesAdapter.AdapterViewHolder adapterViewHolder = new CategoriesAdapter.AdapterViewHolder(view, onClickListener);
        return adapterViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String title = categories.get(position).getTitle();
        TextView textView = ((CategoriesAdapter.AdapterViewHolder) holder).textView;
        textView.setText(title);
    }

    @Override
    public int getItemViewType(int position) {
        if (categories.get(position).parentCategoryId.isEmpty()) return TYPE_CATEGORY;
        else return TYPE_SUBCATEGORY;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
