package com.example.help_hub.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help_hub.OtherClasses.Opinion;
import com.example.help_hub.R;

import java.util.List;

public class OpinionsAdapter extends RecyclerView.Adapter {

    private List<Opinion> opinions;

    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public TextView userNameTextView, opinionTextTextView;
        public RatingBar opinionRatingBar;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTextView = itemView.findViewById(R.id.user_name_text_view);
            opinionRatingBar = itemView.findViewById(R.id.rating_bar);
            opinionTextTextView = itemView.findViewById(R.id.opinion_text_text_view);
        }
    }

    public OpinionsAdapter(List<Opinion> opinions) {
        this.opinions = opinions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.opinion_card_view, parent, false);
        AdapterViewHolder adapterViewHolder = new AdapterViewHolder(view);

        return adapterViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String userName = opinions.get(position).getUserNickname();
        String opinionText = opinions.get(position).getOpinionText();
        Float rating = opinions.get(position).getRating();

        TextView userNameTextView = ((AdapterViewHolder) holder).userNameTextView;
        userNameTextView.setText(userName);

        TextView opinionTextTextView = ((AdapterViewHolder) holder).opinionTextTextView;
        opinionTextTextView.setText(opinionText);

        RatingBar opinionRatingBar = ((AdapterViewHolder) holder).opinionRatingBar;
        opinionRatingBar.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return opinions.size();
    }
}
