package com.example.help_hub.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.AlertDialogues.SelectTypeOfAdvertisement;
import com.example.help_hub.OtherClasses.WantToHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;

public class WantToHelpFragment extends Fragment {

    Activity myActivity;
    Context myContext;
    private List<WantToHelp> wantToHelpList = new LinkedList<>();
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> {
            SelectTypeOfAdvertisement selectTypeOfAdvertisement = new SelectTypeOfAdvertisement(getActivity());
            selectTypeOfAdvertisement.startSelectTypeOfAdvertisement();
        });

        firebaseFirestore.collection("offers").get().addOnSuccessListener(v -> {
            List<DocumentSnapshot> documents = v.getDocuments();
            for (DocumentSnapshot document : documents) {
                WantToHelp wantToHelp = document.toObject(WantToHelp.class);
                wantToHelp.setId(document.getId());
                wantToHelpList.add(wantToHelp);
            }
            recyclerView = view.findViewById(R.id.order_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

            WantToHelpFragment.WantToHelpAdapter adapter = new WantToHelpFragment.WantToHelpAdapter();
            recyclerView.setAdapter(adapter);
        });

        return view;
    }

    private class WantToHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView wantToHelpImage;
        private TextView wantToHelpTitle, wantToHelpPrice, wantToHelpDescription;
        private WantToHelp wantToHelp;

        public WantToHelpHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_want_to_help, parent, false));

            itemView.setOnClickListener(this);

            wantToHelpImage = itemView.findViewById(R.id.want_to_help_image);
            wantToHelpTitle = itemView.findViewById(R.id.want_to_help_title);
            wantToHelpPrice = itemView.findViewById(R.id.want_to_help_price);
            wantToHelpDescription = itemView.findViewById(R.id.want_to_help_description);
        }

        public void bind(WantToHelp wantToHelp) {
            this.wantToHelp = wantToHelp;
            String title, desc;
            title = wantToHelp.getTitle();
            desc = wantToHelp.getDescription();
            if (title.length() > 18)
                title = title.substring(0, 20) + "...";
            if (desc.length() > 30)
                desc = desc.substring(0, 30) + "...";
            wantToHelpTitle.setText(title);
            wantToHelpPrice.setText(getResources().getString(R.string.budget) + " " + wantToHelp.getPrice());
            wantToHelpDescription.setText(desc);
            StorageReference imgRef = storageReference.child("users/" + wantToHelp.getUserId() + "/profile.jpg");
            imgRef.getDownloadUrl().addOnSuccessListener(v -> {
                Glide.with(myContext).load(v).into(wantToHelpImage);
            }).addOnFailureListener(v -> {
                wantToHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
            });
        }

        @Override
        public void onClick(View view) {

        }
    }

    private class WantToHelpAdapter extends RecyclerView.Adapter<WantToHelpFragment.WantToHelpHolder> {

        @NonNull
        @Override
        public WantToHelpFragment.WantToHelpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            return new WantToHelpFragment.WantToHelpHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull WantToHelpFragment.WantToHelpHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return wantToHelpList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull WantToHelpFragment.WantToHelpHolder holder, int position, @NonNull List<Object> payloads) {
            WantToHelp wantToHelp = wantToHelpList.get(position);
            holder.bind(wantToHelp);
        }
    }
}
