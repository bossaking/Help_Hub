package com.example.help_hub.Adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.help_hub.R;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private ClipData clipData;

    public ImageAdapter(Context context, ClipData clipData) {
        this.context = context;
        this.clipData = clipData;
    }

    @Override
    public int getCount() {
        return clipData.getItemCount();
    }

    @Override
    public Object getItem(int position) {
        return clipData.getItemAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridView = convertView;

        if (gridView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.portfolio_photo_layout, null);
        }

        ImageView imageView = gridView.findViewById(R.id.single_portfolio_image);
        imageView.setImageURI(clipData.getItemAt(position).getUri());

        return gridView;
    }
}
