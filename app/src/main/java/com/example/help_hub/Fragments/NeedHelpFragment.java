package com.example.help_hub.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.AddNewNoticeActivity;
import com.example.help_hub.Activities.NeedHelpDetails;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NeedHelpFragment extends Fragment {

    public static final int NEED_HELP_DETAILS_REQUEST_CODE = 1;
    public static final int MAX_PHOTO_LOADING_ATTEMPTS = 20;

    Activity myActivity;
    Context myContext;
    private List<NeedHelp> needHelpList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    NeedHelpAdapter adapter;

    int photoLoadingAttempts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myActivity = getActivity();
        myContext = myActivity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        needHelpList = new ArrayList<>();

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> {
            myActivity.startActivity(new Intent(myContext, AddNewNoticeActivity.class));
        });

        recyclerView = view.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

        adapter = new NeedHelpAdapter();
        recyclerView.setAdapter(adapter);

        firebaseFirestore.collection("announcement").addSnapshotListener((queryDocumentSnapshots, e) -> {

            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        NeedHelp needHelp = dc.getDocument().toObject(NeedHelp.class);
                        needHelp.setId(dc.getDocument().getId());
                        needHelpList.add(needHelp);
                        break;
                }
            }

            adapter.notifyDataSetChanged();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class NeedHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView needHelpImage;
        private TextView needHelpTitle, needHelpPrice, needHelpDescription;
        private NeedHelp needHelp;

        public NeedHelpHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_need_help, parent, false));

            itemView.setOnClickListener(this);

            needHelpImage = itemView.findViewById(R.id.need_help_image);
            needHelpTitle = itemView.findViewById(R.id.need_help_title);
            needHelpPrice = itemView.findViewById(R.id.need_help_price);
            needHelpDescription = itemView.findViewById(R.id.need_help_description);
        }

        public void bind(NeedHelp needHelp) {
            this.needHelp = needHelp;
            String title, desc;
            title = needHelp.getTitle();
            desc = needHelp.getDescription();
            if (title.length() > 18)
                title = title.substring(0, 20) + "...";
            if (desc.length() > 30)
                desc = desc.substring(0, 30) + "...";
            needHelpTitle.setText(title);
            needHelpPrice.setText(getResources().getString(R.string.budget) + " " + needHelp.getPrice() + " " + getString(R.string.new_notice_currency));
            needHelpDescription.setText(desc);

            photoLoadingAttempts = 0;
            getImage();
        }

        private void getImage() {

            photoLoadingAttempts++;
                StorageReference imgRef = storageReference.child("announcement/" + needHelp.getId() + "/images/photo0");
                imgRef.getDownloadUrl().addOnSuccessListener(v -> {
                    Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(needHelpImage);
                }).addOnFailureListener(v -> {
                    needHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
                    if(photoLoadingAttempts != MAX_PHOTO_LOADING_ATTEMPTS)
                    getImage();
                });
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), NeedHelpDetails.class);
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_ID, needHelp.getId());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_TITLE, needHelp.getTitle());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_PRICE, needHelp.getPrice());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_DESCRIPTION, needHelp.getDescription());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_USER_ID, needHelp.getUserId());
            startActivityForResult(intent, NEED_HELP_DETAILS_REQUEST_CODE);
        }
    }

    private class NeedHelpAdapter extends RecyclerView.Adapter<NeedHelpHolder> {

        @NonNull
        @Override
        public NeedHelpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            return new NeedHelpHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull NeedHelpHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return needHelpList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull NeedHelpHolder holder, int position, @NonNull List<Object> payloads) {
            NeedHelp needHelp = needHelpList.get(position);
            holder.bind(needHelp);
        }
    }
}